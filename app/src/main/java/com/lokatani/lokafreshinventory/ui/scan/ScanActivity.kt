package com.lokatani.lokafreshinventory.ui.scan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.customview.BoundingBox
import com.lokatani.lokafreshinventory.data.Result
import com.lokatani.lokafreshinventory.databinding.ActivityScanBinding
import com.lokatani.lokafreshinventory.helper.detectors.Detector
import com.lokatani.lokafreshinventory.ui.detail.DetailActivity
import com.lokatani.lokafreshinventory.utils.Constants.LABELS_PATH
import com.lokatani.lokafreshinventory.utils.Constants.MODEL_PATH
import com.lokatani.lokafreshinventory.utils.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity(), Detector.DetectorListener {
    private lateinit var binding: ActivityScanBinding

    private lateinit var factory: ScanViewModelFactory
    private val scanViewModel: ScanViewModel by viewModels {
        factory
    }

    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private lateinit var cameraExecutor: ExecutorService
    private val isFrontCamera = false

    private var detector: Detector? = null

    private var vegResult: String? = null
    private var vegWeight: Int? = null
    private var lastProcessedBitmap: Bitmap? = null
    private var scaleBitmap: Bitmap? = null
    private var highestConfVegBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        factory = ScanViewModelFactory.getInstance()

        if (allPermissionsGranted()) {
            setupCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        cameraExecutor.execute {
            detector = Detector(baseContext, MODEL_PATH, LABELS_PATH, this) {
                lifecycleScope.launch(Dispatchers.Main) {
                    showToast(it)
                }
            }
        }

        bindListeners()
        observeViewModel()
    }

    private fun bindListeners() {
        binding.apply {
            switchDelegate.setOnCheckedChangeListener { _, isChecked ->
                cameraExecutor.submit {
                    detector?.restart(isGpu = isChecked)
                }
                if (isChecked) {
                    tvSwitchState.text = getString(R.string.gpu)
                } else {
                    tvSwitchState.text = getString(R.string.cpu)
                }
            }

            btnPhoto.setOnClickListener {
                val hasVeggie = highestConfVegBitmap != null
                val hasScale = scaleBitmap != null

                when {
                    // Case 1: Both vegetable and scale are detected
                    hasVeggie && hasScale -> {
                        scanViewModel.recognizeBoth(highestConfVegBitmap!!, scaleBitmap!!)
                    }
                    // Case 2: Only vegetable is detected
                    hasVeggie -> {
                        scanViewModel.recognizeVeggie(highestConfVegBitmap!!)
                    }
                    // Case 3: Only scale is detected
                    hasScale -> {
                        scanViewModel.recognizeText(scaleBitmap!!)
                    }
                    // Case 4: Nothing is detected
                    else -> {
                        val detailIntent = Intent(this@ScanActivity, DetailActivity::class.java)
                        detailIntent.putExtra(
                            DetailActivity.EXTRA_RESULT,
                            "Hasil Local: $vegResult"
                        )
                        detailIntent.putExtra(DetailActivity.EXTRA_WEIGHT, 0)
                        startActivity(detailIntent)
                    }
                }
            }
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
            cameraProvider ?: throw IllegalStateException("Camera initialization failed.")

        val rotation =
            binding.viewFinder.display.rotation

        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()

        preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        imageAnalyzer = ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .setTargetRotation(binding.viewFinder.display.rotation)
            .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    val bitmapBuffer = createBitmap(imageProxy.width, imageProxy.height)
                    imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }

                    val matrix = Matrix().apply {
                        postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
                        if (isFrontCamera) {
                            postScale(
                                -1f,
                                1f,
                                imageProxy.width.toFloat(),
                                imageProxy.height.toFloat()
                            )
                        }
                    }

                    val rotatedBitmap = Bitmap.createBitmap(
                        bitmapBuffer,
                        0,
                        0,
                        bitmapBuffer.width,
                        bitmapBuffer.height,
                        matrix,
                        true
                    )
                    lastProcessedBitmap = rotatedBitmap

                    runOnUiThread {
                        binding.overlay.setPreviewSize(rotatedBitmap.width, rotatedBitmap.height)
                    }

                    detector?.detect(rotatedBitmap)
                    imageProxy.close()
                }
            }

        cameraProvider.unbindAll()

        try {
            camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
            preview?.surfaceProvider = binding.viewFinder.surfaceProvider
        } catch (exc: Exception) {
            Log.e(TAG, "Use case binding failed", exc)
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (it[Manifest.permission.CAMERA] == true) {
            setupCamera()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        imageAnalyzer?.targetRotation = binding.viewFinder.display.rotation
    }

    override fun onResume() {
        super.onResume()
        if (allPermissionsGranted() && cameraProvider == null) {
            setupCamera()
        } else if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
        }
    }

    private fun observeViewModel() {
        scanViewModel.isLoading.observe(this) { isLoading ->
            binding.progressCard.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        scanViewModel.scanResult.observe(this) { result ->
            if (result == null) return@observe

            when (result) {
                is Result.Loading -> {
                    Log.d(TAG, "API Call(s) Loading")
                }

                is Result.Success -> {
                    Log.d(TAG, "API Call(s) Success")
                    val uiState = result.data
                    val intent = Intent(this@ScanActivity, DetailActivity::class.java).apply {
                        putExtra(
                            DetailActivity.EXTRA_RESULT,
                            uiState.vegetableName ?: "Hasil Local: $vegResult"
                        )
                        putExtra(DetailActivity.EXTRA_WEIGHT, uiState.weight ?: 0)
                    }
                    startActivity(intent)
                }

                is Result.Error -> {
                    Log.e(TAG, "API Call(s) Error: ${result.error}")
                    showToast("API call failed: Check your connection or Try again")
                    val intent = Intent(this@ScanActivity, DetailActivity::class.java).apply {
                        putExtra(DetailActivity.EXTRA_RESULT, "Hasil Local: $vegResult")
                        putExtra(DetailActivity.EXTRA_WEIGHT, 0)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onEmptyDetect() {
        runOnUiThread {
            binding.overlay.clear()
            vegResult = null
            vegWeight = null
            updateUIInfo(null)
        }
    }

    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        runOnUiThread {
            binding.tvInterference.text = getString(R.string.ms, inferenceTime)

            binding.overlay.apply {
                setResults(boundingBoxes)
                invalidate()
            }

            // Process detections to update vegResult and attempt OCR for vegWeight
            processDetections(boundingBoxes)
        }
    }

    private fun processDetections(boundingBoxes: List<BoundingBox>) {
        var highestConfVegetable: BoundingBox? = null
        var scaleBox: BoundingBox? = null

        boundingBoxes.forEach { box ->
            when (box.clsName) {
                "Bayam Merah", "Kale" -> {
                    if (highestConfVegetable == null || box.cnf > highestConfVegetable.cnf) {
                        highestConfVegetable = box
                    }
                }

                "Timbangan" -> {
                    if (scaleBox == null || box.cnf > scaleBox.cnf) {
                        scaleBox = box
                    }
                }
            }
        }

        // Update vegetable result
        val newVegResult = highestConfVegetable?.clsName
        vegResult = newVegResult

        // If Vegetable is found, make the bitmap from bounding box
        if (highestConfVegetable != null && lastProcessedBitmap != null) {
            makeVegBitmap(highestConfVegetable, lastProcessedBitmap!!)
        } else {
            highestConfVegBitmap = null
        }

        // If Scale is found, make the bitmap from bounding box
        if (scaleBox != null && lastProcessedBitmap != null) {
            makeOCRBitmap(scaleBox, lastProcessedBitmap!!)
        } else {
            // If no scale is detected, clear scaleBitmap and set vegWeight to null/default
            scaleBitmap = null
            vegWeight = null
        }

        // Update UI with current values
        updateUIInfo(newVegResult)
    }

    private fun makeOCRBitmap(scaleBox: BoundingBox, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val width = bitmap.width
                val height = bitmap.height

                val x1 = (scaleBox.x1 * width).toInt().coerceIn(0, width - 1)
                val y1 = (scaleBox.y1 * height).toInt().coerceIn(0, height - 1)
                val x2 = (scaleBox.x2 * width).toInt().coerceIn(x1, width)
                val y2 = (scaleBox.y2 * height).toInt().coerceIn(y1, height)

                Log.d("Bitmap Size", "Bitmap height: $height, width: $width")
                Log.d("OCR Region", "y1: $y1, y2: $y2, x1: $x1, x2: $x2")

                val roiWidth = x2 - x1
                val roiHeight = y2 - y1

                if (roiWidth > 0 && roiHeight > 0) {
                    scaleBitmap = Bitmap.createBitmap(
                        bitmap,
                        x1,
                        y1,
                        roiWidth,
                        roiHeight
                    )
                } else {
                    Log.w(TAG, "ROI has zero or negative dimensions, cannot create bitmap.")
                    scaleBitmap = null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing or creating bitmap", e)
                scaleBitmap = null
            }
        }
    }

    private fun makeVegBitmap(vegBox: BoundingBox, bitmap: Bitmap) {
        lifecycleScope.launch {
            try {
                val width = bitmap.width
                val height = bitmap.height

                val x1 = (vegBox.x1 * width).toInt().coerceIn(0, width - 1)
                val y1 = (vegBox.y1 * height).toInt().coerceIn(0, height - 1)
                val x2 = (vegBox.x2 * width).toInt().coerceIn(x1, width)
                val y2 = (vegBox.y2 * height).toInt().coerceIn(y1, height)

                Log.d("Bitmap Size", "Bitmap height: $height, width: $width")
                Log.d("Vegetable Region", "y1: $y1, y2: $y2, x1: $x1, x2: $x2")

                val roiWidth = x2 - x1
                val roiHeight = y2 - y1

                if (roiWidth > 0 && roiHeight > 0) {
                    highestConfVegBitmap = Bitmap.createBitmap(
                        bitmap,
                        x1,
                        y1,
                        roiWidth,
                        roiHeight
                    )
                } else {
                    Log.w(TAG, "ROI has zero or negative dimensions, cannot create bitmap.")
                    highestConfVegBitmap = null
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing or creating bitmap", e)
                highestConfVegBitmap = null
            }
        }
    }

    private fun updateUIInfo(vegetable: String?) {
        binding.scanInfo.tvJenis.text = vegetable ?: getString(R.string.no_data)
    }

    companion object {
        private const val TAG = "ScanActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).toTypedArray()
    }
}