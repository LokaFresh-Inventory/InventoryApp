package com.lokatani.lokafreshinventory.customview.custombutton

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.withStyledAttributes
import com.lokatani.lokafreshinventory.R

class ButtonWithTralingIcon @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val leadingIcon: ImageView
    private val trailingIcon: ImageView
    private val buttonText: TextView

    init {
        LayoutInflater.from(context).inflate(R.layout.button_with_trailing_icon, this, true)
        leadingIcon = findViewById(R.id.leading_icon)
        trailingIcon = findViewById(R.id.trailing_icon)
        buttonText = findViewById(R.id.tv_text)

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.ButtonWithTralingIcon, 0, 0) {
                val text = getString(R.styleable.ButtonWithTralingIcon_text) ?: ""
                val leadingIconRes = getResourceId(R.styleable.ButtonWithTralingIcon_leadingIcon, 0)
                val trailingIconRes =
                    getResourceId(R.styleable.ButtonWithTralingIcon_trailingIcon, 0)

                buttonText.text = text
                if (leadingIconRes != 0) leadingIcon.setImageResource(leadingIconRes)
                if (trailingIconRes != 0) trailingIcon.setImageResource(trailingIconRes)

            }
        }
    }

}