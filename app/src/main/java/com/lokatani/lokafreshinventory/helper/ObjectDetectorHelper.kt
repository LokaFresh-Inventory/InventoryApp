package com.lokatani.lokafreshinventory.helper

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import com.lokatani.lokafreshinventory.helper.detectors.ObjectDetection
import com.lokatani.lokafreshinventory.helper.detectors.ObjectDetector
import com.lokatani.lokafreshinventory.helper.detectors.YoloDetector
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.Rot90Op

class ObjectDetectorHelper(
    var threshold: Float = 0.5f,
    var numThreads: Int = 2,
    var maxResults: Int = 3,
    var currentDelegate: Int = 0,
    var currentModel: Int = 0,
    val context: Context,
    val objectDetectorListener: DetectorListener?
) {

    // For this example this needs to be a var so it can be reset on changes. If the ObjectDetector
    // will not change, a lazy val would be preferable.
    private var objectDetector: ObjectDetector? = null

    init {
        setupObjectDetector()
    }

    fun setupObjectDetector() {
        try {
            if (currentModel == MODEL_YOLO) {
                objectDetector = YoloDetector(
                    threshold,
                    0.3f,
                    numThreads,
                    maxResults,
                    currentDelegate,
                    currentModel,
                    context,
                )
            }
        } catch (e: Exception) {

            objectDetectorListener?.onError(e.toString())

        }


    }


    fun detect(image: Bitmap, imageRotation: Int) {

        if (objectDetector == null) {
            setupObjectDetector()
        }

        // Create preprocessor for the image.
        // See https://www.tensorflow.org/lite/inference_with_metadata/lite_support#imageprocessor_architecture

        val imageProcessor =
            ImageProcessor.Builder()
                .add(Rot90Op(-imageRotation / 90))
                .build()

        // Preprocess the image and convert it into a TensorImage for detection.
        val tensorImage = imageProcessor.process(TensorImage.fromBitmap(image))

        // Inference time is the difference between the system time at the start and finish of the
        // process
        var inferenceTime = SystemClock.uptimeMillis()

        val results = objectDetector?.detect(tensorImage, imageRotation)

        inferenceTime = SystemClock.uptimeMillis() - inferenceTime

        if (results != null) {
            objectDetectorListener?.onResults(
                results.detections,
                inferenceTime,
                results.image.height,
                results.image.width
            )
        }

    }

    interface DetectorListener {
        fun onError(error: String)
        fun onResults(
            results: MutableList<ObjectDetection>?,
            inferenceTime: Long,
            imageHeight: Int,
            imageWidth: Int
        )
    }

    companion object {
        const val MODEL_YOLO = 4
    }
}
