package levkaantonov.com.study.customviewgroup

import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.BaseInterpolator
import androidx.core.content.withStyledAttributes

class CustomView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 80f }
    private var textLayout: Layout? = null
    private var editable: Editable = SpannableStringBuilder()

    private var verticalOffset = 0

    private val animator = ValueAnimator
        .ofObject(StringEvaluator(), "Привет!", "Привет! Как дела? Как настроение?")
        .apply {
            duration = 4000L
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            interpolator = TwoStepsInterpolator()
            addUpdateListener { animator ->
                val animatedValue = animator.animatedValue.toString()

                val prevLineCount = textLayout?.lineCount
                editable.replace(0, editable.length, animatedValue)
                if (textLayout?.lineCount != prevLineCount) {
                    requestLayout()
                }
                invalidate()
            }
        }

    init {
        context.withStyledAttributes(attrs, R.styleable.CustomViewGroup, defStyleAttr) {
            verticalOffset = getDimensionPixelOffset(R.styleable.CustomViewGroup_verticalOffset, 0)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        animator.start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator.cancel()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val width = if (widthSize > 0) widthSize else 500
        val height = textLayout?.height ?: (textPaint.descent() - textPaint.ascent()).toInt()

        setMeasuredDimension(width, height)
    }

    @SuppressLint("NewApi")
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (w == oldw) return

        textLayout = DynamicLayout.Builder
            .obtain(editable, textPaint, w)
            .build()
    }

    override fun onDraw(canvas: Canvas?) {
        textLayout?.draw(canvas)
    }
}

class StringEvaluator : TypeEvaluator<String> {
    override fun evaluate(p0: Float, startValue: String, endValue: String): String {
        val coercedFraction = p0.coerceIn(0f, 1f)

        val lengthDiff = endValue.length - startValue.length
        val currentDiff = (lengthDiff * coercedFraction).toInt()
        return if (currentDiff > 0) {
            endValue.substring(0, startValue.length + currentDiff)
        } else {
            startValue.substring(0, startValue.length + currentDiff)
        }
    }
}

@SuppressLint("NewApi")
class TwoStepsInterpolator : BaseInterpolator() {
    override fun getInterpolation(input: Float): Float {
        return when {
            input < 0.3f -> 0.5f * (input / 0.3f)
            input > 0.7f -> 0.5f + (0.5f * (input - 0.7f) / 0.3f)
            else -> 0.5f
        }
    }

}