package com.devzone.fillprogresslayout

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat


@Suppress("unused")
class FillProgressLayout : LinearLayout {

    companion object {
        const val LEFT_TO_RIGHT = 0
        const val RIGHT_TO_LEFT = 1
        const val TOP_TO_BOTTOM = 2
        const val BOTTOM_TO_TOP = 3
    }

    //default values
    private val maxProgress = 100
    private val defDurationFactor = 30
    private val defDuration = defDurationFactor * 100 //3000 ms or 3 second (for 0 to 100 progress)
    private val defCornerRadius = 20f
    private val defDirection = LEFT_TO_RIGHT
    private val defBackgroundColor = Color.LTGRAY
    private val defProgressColor = Color.GRAY
    private val defIsRestart = false
    private val defIsRounded = false

    // customisable values
    private var isRounded = defIsRounded
    private var isRestart = defIsRestart

    private var mDurationFactor = defDurationFactor
    private var mDirection = defDirection
    private var mCornerRadius = defCornerRadius
    private var mBackgroundColor = defBackgroundColor
    private var mProgressColor = defProgressColor

    private var oldProgress = 0
    private var currentProgress = 0


    //drawing assets
    private var progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()
    private var progressRectF = RectF()
    private var backRectF = RectF()
    private var clipProgressPath = Path()


    constructor(context: Context) : super(context) {
        initUI(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        initUI(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initUI(context, attrs)
    }

    private fun initUI(context: Context, attrs: AttributeSet?) {
        setWillNotDraw(false)

        attrs?.let {
            val array: TypedArray = context.obtainStyledAttributes(it, R.styleable.FillProgressLayout)
            if (array.length() > 0) {
                mBackgroundColor = array.getColor(
                    R.styleable.FillProgressLayout_fpl_backgroundColor,
                    defBackgroundColor
                )
                mProgressColor = array.getColor(
                    R.styleable.FillProgressLayout_fpl_progressColor,
                    defProgressColor
                )

                val restart = array.getBoolean(R.styleable.FillProgressLayout_fpl_shouldRestart, defIsRestart)
                shouldStartFromZero(restart)

                val cornerRadius =
                    array.getFloat(R.styleable.FillProgressLayout_fpl_roundedCornerRadius, defCornerRadius)
                setCornerRadius(cornerRadius)

                val isRounded = array.getBoolean(R.styleable.FillProgressLayout_fpl_isRounded, defIsRounded)
                setRoundedCorners(isRounded)

                val duration = array.getInt(R.styleable.FillProgressLayout_fpl_progressDuration, defDuration)
                setDuration(duration.toLong())

                val direction = array.getInt(R.styleable.FillProgressLayout_fpl_progressDirection, defDirection)
                setDirection(direction)

                val progress =
                    array.getInt(R.styleable.FillProgressLayout_fpl_progress, currentProgress)
                setProgress(progress)
            }
            array.recycle()
        }
        initPaint()
    }

    private fun initPaint() {
        backgroundPaint.apply {
            this.style = Paint.Style.FILL
            this.color = mBackgroundColor
        }
        progressPaint.apply {
            this.style = Paint.Style.FILL
            this.color = mProgressColor
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        backRectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        progressRectF = RectF(0f, 0f, w.toFloat(), h.toFloat())
        progressRectF.bottom = h.toFloat()
        updateRect(progressRectF)
        if (isRounded) {
            clipPath.addRoundRect(backRectF, mCornerRadius, mCornerRadius, Path.Direction.CW)
            clipPath.close()
            clipProgressPath.set(clipPath)
        }

    }

    private fun getProgress() = getSize() * currentProgress / 100

    private fun getSize() = if (mDirection == LEFT_TO_RIGHT || mDirection == RIGHT_TO_LEFT) width else height

    private fun updateRect(rectF: RectF) {
        when (mDirection) {
            LEFT_TO_RIGHT -> rectF.right = getProgress().toFloat()
            RIGHT_TO_LEFT -> rectF.left = getSize() - getProgress().toFloat()
            TOP_TO_BOTTOM -> rectF.bottom = getProgress().toFloat()
            BOTTOM_TO_TOP -> rectF.top = getSize() - getProgress().toFloat()
        }
    }


    override fun onDraw(canvas: Canvas?) {
        if (isRounded) {
            drawRoundedProgress(canvas)
        } else {
            drawNormalProgress(canvas)
        }
        super.onDraw(canvas)
    }

    private fun drawNormalProgress(canvas: Canvas?) {
        canvas?.apply {
            drawRect(backRectF, backgroundPaint)
            drawRect(progressRectF, progressPaint)
        }
    }

    private fun drawRoundedProgress(canvas: Canvas?) {
        canvas?.apply {
            save()
            drawRoundRect(backRectF, mCornerRadius, mCornerRadius, backgroundPaint)
            clipPath(clipProgressPath, Region.Op.INTERSECT)
            drawRoundRect(progressRectF, mCornerRadius, mCornerRadius, progressPaint)
            restore()
        }
    }

    override fun dispatchDraw(canvas: Canvas?) {
        if (isRounded)
            canvas?.clipPath(clipPath)
        super.dispatchDraw(canvas)
    }

    private fun isValidRes(res: Int) = res != View.NO_ID

    override fun onDetachedFromWindow() {
        clearAnimation()
        super.onDetachedFromWindow()
    }

//---------------------public setters--------------------------------------------------------------------//

    fun setProgress(p: Int) {
        if (p in 0..maxProgress) {
            clearAnimation()
            val animator = ValueAnimator.ofInt(oldProgress, p)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.addUpdateListener { anm ->
                currentProgress = anm.animatedValue as Int
                updateRect(rectF = progressRectF)
                ViewCompat.postInvalidateOnAnimation(this)
            }
            animator.doOnEnd { if (!isRestart) oldProgress = p }
            animator.setDuration(((kotlin.math.abs(p - oldProgress)) * mDurationFactor).toLong()).start()
        }
    }

    fun setProgressBackgroundColor(@ColorRes resId: Int) {
        if (isValidRes(resId)) {
            mBackgroundColor = ContextCompat.getColor(context, resId)
        }
    }

    fun setProgressColor(@ColorRes resId: Int) {
        if (isValidRes(resId)) {
            mProgressColor = ContextCompat.getColor(context, resId)
        }
    }


    fun setCornerRadius(radius: Float) {
        if (radius in 0f..maxProgress.toFloat()) {
            setRoundedCorners(true)
            this.mCornerRadius = radius
        }
    }


    fun setRoundedCorners(isRounded: Boolean) {
        this.isRounded = isRounded
    }

    fun setDuration(duration: Long) {
        if (duration.toInt() == 0 || duration < 0) return
        mDurationFactor = (duration / 100).toInt()
    }

    fun shouldStartFromZero(isRestart: Boolean) {
        this.isRestart = isRestart
    }

    fun setDirection(direction: Int) {
        mDirection = if (direction in LEFT_TO_RIGHT..BOTTOM_TO_TOP) direction else defDirection
    }


    //--------------------------------------------------------------------- --------------------------------------------//
    // default background setters overridden as we have our own background color implementation [#backRectF]
    override fun setBackground(background: Drawable?) {}

    override fun setBackgroundColor(color: Int) {}

    override fun setBackgroundResource(resid: Int) {}

    override fun setBackgroundDrawable(background: Drawable?) {}
    //-----------------------------------------------------------------------------------------------------------------//


}