package com.lokatani.lokafreshinventory.customview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.lokatani.lokafreshinventory.R

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results = listOf<BoundingBox>()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var bounds = Rect()

    // Keep track of actual preview dimensions vs view dimensions
    private var previewWidth = 0
    private var previewHeight = 0
    private var scaleX = 1.0f
    private var scaleY = 1.0f
    private var offsetX = 0.0f
    private var offsetY = 0.0f

    init {
        initPaints()
    }

    fun clear() {
        results = listOf()
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.bounding_box_color)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    fun setPreviewSize(width: Int, height: Int) {
        previewWidth = width
        previewHeight = height
        updateScaleAndOffset()
    }

    private fun updateScaleAndOffset() {
        if (previewWidth <= 0 || previewHeight <= 0 || width <= 0 || height <= 0) return

        // Calculate aspect ratios
        val viewAspectRatio = width.toFloat() / height.toFloat()
        val previewAspectRatio = previewWidth.toFloat() / previewHeight.toFloat()

        // Calculate scale factors and offsets based on aspect ratios
        if (viewAspectRatio > previewAspectRatio) {
            // View is wider than preview
            scaleY = height.toFloat() / previewHeight.toFloat()
            scaleX = scaleY
            offsetX = (width - previewWidth * scaleX) / 2.0f
            offsetY = 0.0f
        } else {
            // View is taller than preview
            scaleX = width.toFloat() / previewWidth.toFloat()
            scaleY = scaleX
            offsetY = (height - previewHeight * scaleY) / 2.0f
            offsetX = 0.0f
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateScaleAndOffset()
    }

    @SuppressLint("DefaultLocale")
    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        results.forEach {
            // Map normalized coordinates (0-1) to view coordinates with proper scaling and offset
            val left = it.x1 * previewWidth * scaleX + offsetX
            val top = it.y1 * previewHeight * scaleY + offsetY
            val right = it.x2 * previewWidth * scaleX + offsetX
            val bottom = it.y2 * previewHeight * scaleY + offsetY

            // Create rectangle for drawing
            val rect = RectF(left, top, right, bottom)
            canvas.drawRect(rect, boxPaint)

            // Draw text label
            val drawableText = "${it.clsName}: ${String.format("%.2f", it.cnf)}"
            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()

            canvas.drawRect(
                left,
                top - textHeight - BOUNDING_RECT_TEXT_PADDING,
                left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                top,
                textBackgroundPaint
            )
            canvas.drawText(
                drawableText,
                left + BOUNDING_RECT_TEXT_PADDING / 2,
                top - BOUNDING_RECT_TEXT_PADDING / 2,
                textPaint
            )
        }
    }

    fun setResults(boundingBoxes: List<BoundingBox>) {
        results = boundingBoxes
        invalidate()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}