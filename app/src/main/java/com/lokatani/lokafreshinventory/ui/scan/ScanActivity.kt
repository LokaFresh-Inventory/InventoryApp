package com.lokatani.lokafreshinventory.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio.RATIO_4_3
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
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityScanBinding
import com.lokatani.lokafreshinventory.helper.ObjectDetectorHelper
import com.lokatani.lokafreshinventory.helper.detectors.ObjectDetection
import com.lokatani.lokafreshinventory.utils.ViewModelFactory
import com.lokatani.lokafreshinventory.utils.showToast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

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
        hideSystemUI()

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
            .setTargetAspectRatio(RATIO_4_3)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setTargetAspectRatio(RATIO_4_3)
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
                    scanInfo.tvJenis.text = results[0].category.label.toString()
                    scanInfo.tvBerat.text = results[0].category.confidence.toString()
                } else {
                    scanInfo.tvJenis.text = getString(R.string.no_data)
                    scanInfo.tvBerat.text = getString(R.string.no_data)
                }

                tvInterference.text = String.format(Locale.getDefault(), "%d ms", inferenceTime)
                results?.let {
                    overlay.setResults(
                        it,
                        imageHeight,
                        imageWidth
                    )
                    overlay.invalidate()

                }
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

    private fun hideSystemUI() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    override fun onDestroy() {
        super.onDestroy()

        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "ScanActivity"
    }
}