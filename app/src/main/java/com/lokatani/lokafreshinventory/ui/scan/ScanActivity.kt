package com.lokatani.lokafreshinventory.ui.scan

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lokatani.lokafreshinventory.R
import com.lokatani.lokafreshinventory.databinding.ActivityScanBinding
import com.lokatani.lokafreshinventory.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScanBinding
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        binding.btnPhoto.setOnClickListener {
            val view = View.inflate(this, R.layout.take_photo_alert, null)
            val resultDialog = MaterialAlertDialogBuilder(this)
                .setView(view)
                .setCancelable(false)

            val dialog = resultDialog.show()

            val closeButton = view.findViewById<MaterialButton>(R.id.btn_close)
            closeButton.setOnClickListener {
                dialog.dismiss()
            }

            val buttonSave = view.findViewById<MaterialButton>(R.id.btn_save)
            buttonSave.setOnClickListener {
                TODO()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUI()
        startCamera()
    }

    private fun startCamera() {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            classifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@ScanActivity, error, Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Classification error: $error")
                    }
                }

                override fun onResult(result: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        result?.let { classifications ->
                            if (classifications.isNotEmpty() && classifications[0].categories.isNotEmpty()) {
                                // Get sorted categories (sort happens in the helper now)
                                val topCategory = classifications[0].categories[0]

                                Log.d(
                                    TAG,
                                    "Top result: ${topCategory.label} (${topCategory.score})"
                                )
                                binding.apply {
                                    tvJenis.text = "Vegetable: ${topCategory.label}"
                                    // You would need to implement weight detection separately
                                    tvBerat.text = "Weight: Calculating..."
                                    tvInterference.text = "$inferenceTime ms"
                                }
                            } else {
                                binding.apply {
                                    tvJenis.text = "Not Detected"
                                    tvBerat.text = "Not Detected"
                                    tvInterference.text = "$inferenceTime ms"
                                }
                            }
                        }
                    }
                }
            }
        )

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val resolutionSelector = ResolutionSelector.Builder()
                .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
                .build()
            val imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
            imageAnalyzer.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                imageClassifierHelper.classifyImage(image)
            }

            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.viewFinder.surfaceProvider
            }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(
                    this@ScanActivity,
                    "Failed to find camera",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "Start Camera: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(this))
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

    companion object {
        private const val TAG = "ScanActivity"
    }
}