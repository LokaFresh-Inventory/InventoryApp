package com.lokatani.lokafreshinventory.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityScanBinding
import com.lokatani.lokafreshinventory.helper.ObjectDetectorHelper
import com.lokatani.lokafreshinventory.helper.detectors.ObjectDetection
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import com.lokatani.lokafreshinventory.utils.showToast
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.max

class ScanActivity : AppCompatActivity(), ObjectDetectorHelper.DetectorListener {
    private lateinit var binding: ActivityScanBinding
    private lateinit var objectDetectorHelper: ObjectDetectorHelper
    private lateinit var bitmapBuffer: Bitmap
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var scanFactory: ViewModelFactory
    private val scanViewModel: ScanViewModel by viewModels {
        scanFactory
    }

    private var scaleFactor: Float = 1f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        scanFactory = ViewModelFactory.getInstance(this)

        binding.btnPhoto.setOnClickListener {
            val view = View.inflate(this, R.layout.take_photo_alert, null)
            val resultDialog = MaterialAlertDialogBuilder(this)
                .setView(view)

            val dialog = resultDialog.show()
            dialog.setCanceledOnTouchOutside(false)

            val resultVegType = view.findViewById<TextView>(R.id.tv_result_type)
            resultVegType.text = binding.scanInfo.tvJenis.text

            val resultVegWeight = view.findViewById<TextView>(R.id.tv_result_weight)
            val rawResultVegWeight = binding.scanInfo.tvBerat.text.toString().toFloatOrNull() ?: 0f
            resultVegWeight.text =
                String.format(Locale.getDefault(), "%.2f gram", rawResultVegWeight)
            val closeButton = view.findViewById<MaterialButton>(R.id.btn_close)
            closeButton.setOnClickListener {
                dialog.dismiss()
            }

            val buttonSave = view.findViewById<MaterialButton>(R.id.btn_save)

            val currentDate = getCurrentDate()
            buttonSave.setOnClickListener {
                scanViewModel.insertResult(
                    user = "Test User",
                    vegResult = resultVegType.text.toString(),
                    vegWeight = rawResultVegWeight,
                    date = currentDate
                )
            }

            scanViewModel.insertCompleted.observe(this) { completed ->
                if (completed == true) {
                    dialog.dismiss()
                    scanViewModel.resetInsertStatus()
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()

        objectDetectorHelper = ObjectDetectorHelper(
            context = this@ScanActivity,
            objectDetectorListener = this
        )

        objectDetectorHelper.threshold = 0.4f
        objectDetectorHelper.maxResults = 2
        objectDetectorHelper.numThreads = 2
        objectDetectorHelper.currentDelegate = 1
        objectDetectorHelper.currentModel = 4

        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.viewFinder.post {
            setupCamera()
        }
    }

    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
                bindCameraUseCases()
            }, ContextCompat.getMainExecutor(this)
        )
    }

    private fun bindCameraUseCases() {
        val cameraProvider =
            cameraProvider ?: throw IllegalStateException("Camera initialization failed")

        val cameraSelector = CameraSelector.Builder().requireLensFacing(LENS_FACING_BACK).build()

        preview = Preview.Builder()
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
            .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()

            .also {
                it.setAnalyzer(cameraExecutor) { image ->
                    if (!::bitmapBuffer.isInitialized) {
                        bitmapBuffer = createBitmap(image.width, image.height)
                    }

                    detectObjects(image)
                }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)

            preview?.surfaceProvider = binding.viewFinder.surfaceProvider
        } catch (e: Exception) {
            Log.e(TAG, "Use case binding failed", e)
        }
    }

    private fun detectObjects(image: ImageProxy) {
        // Copy out RGB bits to the shared bitmap buffer
        image.use {
            bitmapBuffer.copyPixelsFromBuffer(image.planes[0].buffer)
        }

        val imageRotation = image.imageInfo.rotationDegrees
        // Pass Bitmap and rotation to the object detector helper for processing and detection
        objectDetectorHelper.detect(bitmapBuffer, imageRotation)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = binding.viewFinder.display.rotation
    }

    override fun onResults(
        results: MutableList<ObjectDetection>?,
        inferenceTime: Long,
        imageHeight: Int,
        imageWidth: Int
    ) {
        runOnUiThread {
            binding.apply {
                if (!results.isNullOrEmpty()) {
                    // Filter sayur dan timbangan
                    val sayurList = results.filter {
                        it.category.label == "bayam merah" || it.category.label == "kale"
                    }
                    val timbangan = results.find {
                        it.category.label == "timbangan"
                    }

                    // Ambil sayur dengan confidence tertinggi
                    val bestSayur = sayurList.maxByOrNull { it.category.confidence }

                    scanInfo.tvJenis.text =
                        bestSayur?.category?.label ?: getString(R.string.no_data)

                    // OCR: Ambil bounding box dari timbangan (nanti digunakan untuk crop bitmap)
                    timbangan?.boundingBox?.let { box ->
                        Log.d(TAG, "Bounding box: $box")

                        try {
                            bitmapBuffer.let { buffer -> // Use safe call here
                                Log.d(TAG, "bitmapBuffer width ${buffer.width} x ${buffer.height}")

                                // First, rotate the original bitmap by 90 degrees
                                val matrix = android.graphics.Matrix()
                                matrix.postRotate(90f)
                                val rotatedBuffer = Bitmap.createBitmap(
                                    buffer,
                                    0,
                                    0,
                                    buffer.width,
                                    buffer.height,
                                    matrix,
                                    true
                                )

                                // Now calculate scaling factor based on the rotated bitmap dimensions
                                // Note that width and height are flipped due to rotation
                                scaleFactor = max(
                                    rotatedBuffer.width.toFloat() * 1f / imageHeight,
                                    rotatedBuffer.height.toFloat() * 1f / imageWidth
                                )

                                val left = (box.left * scaleFactor).toInt()
                                val top = (box.top * scaleFactor).toInt()
                                val right = (box.right * scaleFactor).toInt()
                                val bottom = (box.bottom * scaleFactor).toInt()
                                Log.d(
                                    TAG,
                                    "Left : $left, Right: $right, Top: $top, Bottom: $bottom"
                                )

                                // Ensure the calculated coordinates and dimensions are valid
                                val croppedWidth = right - left
                                val croppedHeight = bottom - top

                                if (left >= 0 && top >= 0 && croppedWidth > 0 && croppedHeight > 0 &&
                                    right <= rotatedBuffer.width && bottom <= rotatedBuffer.height
                                ) {

                                    val timbanganBitmap = Bitmap.createBitmap(
                                        rotatedBuffer,
                                        left,
                                        top,
                                        croppedWidth,
                                        croppedHeight
                                    )

                                    val file = File(getExternalFilesDir(null), "timbangan_crop.jpg")
                                    val out = FileOutputStream(file)
                                    timbanganBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                    out.flush()
                                    out.close()
                                    Log.d(TAG, "Saved cropped image to: ${file.absolutePath}")
                                    processOCR(timbanganBitmap)
                                } else {
                                    Log.e(
                                        TAG, "Error: Calculated crop region is out of bounds. " +
                                                "Left: $left, Top: $top, Right: $right, Bottom: $bottom, " +
                                                "Buffer Width: ${rotatedBuffer.width}, Buffer Height: ${rotatedBuffer.height}"
                                    )
                                    binding.scanInfo.tvBerat.text = getString(R.string.no_data)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Error cropping bitmap: ${e.message}")
                            binding.scanInfo.tvBerat.text = getString(R.string.no_data)
                        }
                    }


                    // Render ke overlay
                    overlay.setResults(results, imageHeight, imageWidth)
                    overlay.invalidate()
                } else {
                    scanInfo.tvJenis.text = getString(R.string.no_data)
                    scanInfo.tvBerat.text = getString(R.string.no_data)
                }

                tvInterference.text = String.format(Locale.getDefault(), "%d ms", inferenceTime)
            }
        }
    }

    private fun processOCR(bitmap: Bitmap) {
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        val image = InputImage.fromBitmap(bitmap, 0)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedText = visionText.text
                Log.d(TAG, "Detected text: $detectedText")

                // Ambil hanya angka/berat jika memungkinkan
                val weightMatch = Regex("""\d+(\.\d+)?""").find(detectedText)
                val weight = weightMatch?.value ?: getString(R.string.no_data)

                runOnUiThread {
                    binding.scanInfo.tvBerat.text = getString(R.string.gram, weight)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "OCR failed: ${e.message}")
                runOnUiThread {
                    binding.scanInfo.tvBerat.text = getString(R.string.no_data)
                }
            }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = Date()
        return dateFormat.format(date)
    }

    override fun onError(error: String) {
        runOnUiThread {
            showToast(error)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "ScanActivity"
    }
}