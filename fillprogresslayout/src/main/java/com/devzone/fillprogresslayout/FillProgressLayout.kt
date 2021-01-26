package com.devzone.fillprogresslayout

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
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

        //specific for gradient direction
        const val TOP_LEFT_TO_BOTTOM_RIGHT = 4
        const val TOP_RIGHT_TO_BOTTOM_LEFT = 5
        const val BOTTOM_RIGHT_TO_TOP_LEFT = 6
        const val BOTTOM_LEFT_TO_TOP_RIGHT = 7
    }

    //default values
    private val maxProgress = 100
    private val defDurationFactor = 20
    private val defDuration = defDurationFactor * 100 //3000 ms or 3 second (for 0 to 100 progress)
    private val defCornerRadius = 20f
    private val defDirection = LEFT_TO_RIGHT
    private val defBackgroundColor = Color.LTGRAY
    private val defProgressColor = Color.GRAY
    private val defIsRestart = false
    private val defIsRounded = false
    private val defGradientMovement = false
    private val defAnimInterpolator = AccelerateDecelerateInterpolator()

    // customisable values
    private var isRounded = defIsRounded
    private var isRestart =
        defIsRestart // restart progress from 0 if true else from last progress value
    private var gradientMovement =
        defGradientMovement // gradient color move with progress if true else stay static

    private var mDurationFactor = defDurationFactor
    private var mDirection = defDirection
    private var mCornerRadius = defCornerRadius
    private var mBackgroundColor = defBackgroundColor
    private var mProgressColor = defProgressColor
    private var mGradientDirection = defDirection
    private var mGradientColors = intArrayOf()

    private var animInterpolator: TimeInterpolator = defAnimInterpolator
    var mAnimator: ValueAnimator? = null // progress animator
        private set
    private var oldProgress = 0
    var currentProgress = 0
        private set


    //drawing assets
    private var progressPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()
    private var progressRectF = RectF()
    private var backRectF = RectF()
    private var doOnProgressEnd: ((v: View) -> Unit)? = null
    private var progressUpdateListener: ((progress: Int) -> Unit)? = null


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
            val array: TypedArray =
                context.obtainStyledAttributes(it, R.styleable.FillProgressLayout)
            if (array.length() > 0) {
                mBackgroundColor = array.getColor(
                    R.styleable.FillProgressLayout_fpl_backgroundColor,
                    defBackgroundColor
                )
                mProgressColor = array.getColor(
                    R.styleable.FillProgressLayout_fpl_progressColor,
                    defProgressColor
                )

                val restart =
                    array.getBoolean(R.styleable.FillProgressLayout_fpl_shouldRestart, defIsRestart)
                shouldStartFromZero(restart)

                val cornerRadius =
                    array.getFloat(
                        R.styleable.FillProgressLayout_fpl_roundedCornerRadius,
                        defCornerRadius
                    )
                setCornerRadius(cornerRadius)

                val isRounded =
                    array.getBoolean(R.styleable.FillProgressLayout_fpl_isRounded, defIsRounded)
                setRoundedCorners(isRounded)

                val duration =
                    array.getInt(R.styleable.FillProgressLayout_fpl_progressDuration, defDuration)
                setDuration(duration.toLong())

                val direction =
                    array.getInt(R.styleable.FillProgressLayout_fpl_progressDirection, defDirection)
                setFillDirection(direction)

                val progress =
                    array.getInt(R.styleable.FillProgressLayout_fpl_progress, currentProgress)
                setProgress(progress)

                val gradDirection =
                    array.getInt(R.styleable.FillProgressLayout_fpl_gradientDirection, defDirection)
                setGradientDirection(gradDirection)

                val gradMovement =
                    array.getBoolean(
                        R.styleable.FillProgressLayout_fpl_gradientMovement,
                        defGradientMovement
                    )
                setGradientMovement(gradMovement)

                try {
                    val colorsId =
                        array.getResourceId(R.styleable.FillProgressLayout_fpl_gradientColors, 0)
                    val gradColors = array.resources.getIntArray(colorsId)
                    if (gradColors.isNotEmpty())
                        setProgressColors(gradColors, false)
                } catch (e: Exception) {
                    log("Error setting Gradient colors! Use @array/colors or int array of R.color.colorName values")
                }
            }
            array.recycle()
        }
        initPaint()
    }

    private fun initPaint() {
        backgroundPaint.apply {
            style = Paint.Style.FILL
            color = mBackgroundColor
        }
        progressPaint.apply {
            style = Paint.Style.FILL
            color = if (mGradientColors.isEmpty()) mProgressColor else Color.BLACK
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
        }

    }

    private fun getProgress() = getSize() * currentProgress / 100

    private fun getSize() =
        if (mDirection == LEFT_TO_RIGHT || mDirection == RIGHT_TO_LEFT) width else height

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
            applyGradientIfAny()
            drawRect(progressRectF, progressPaint)
        }
    }

    private fun drawRoundedProgress(canvas: Canvas?) {
        canvas?.apply {
            save()
            drawRoundRect(backRectF, mCornerRadius, mCornerRadius, backgroundPaint)
            clipPath(clipPath)
            applyGradientIfAny()
            drawRect(progressRectF, progressPaint)
            restore()
        }
    }

    private fun applyGradientIfAny() {
        if (mGradientColors.isNotEmpty()) {
            val gradientRect = getGradientRect(if (gradientMovement) progressRectF else backRectF)
            progressPaint.shader = LinearGradient(
                gradientRect.left,//x0
                gradientRect.top,//y0
                gradientRect.right,//x1
                gradientRect.bottom,//y1
                mGradientColors,
                null,
                Shader.TileMode.MIRROR
            )
        }
    }


    private fun isValidRes(res: Int) = res != View.NO_ID

    override fun onDetachedFromWindow() {
        clearAnimation()
        super.onDetachedFromWindow()
    }

    override fun dispatchDraw(canvas: Canvas?) { // child clipping done here
        if (isRounded)
            canvas?.clipPath(clipPath)
        super.dispatchDraw(canvas)
    }

//---------------------public setters--------------------------------------------------------------------//

    /**
     * This method is used to set the current progress value externally
     * with or without animation
     * @param inputProgress current progress value
     * @param animated should animate filling
     */
    fun setProgress(inputProgress: Int, animated: Boolean = true) {
        if (inputProgress in 0..maxProgress) {
            if (mAnimator?.isRunning == true)
                mAnimator?.end()
            clearAnimation()
            if (animated) {
                mAnimator = ValueAnimator.ofInt(oldProgress, inputProgress).apply {
                    interpolator = animInterpolator
                    addUpdateListener { anm ->
                        currentProgress = anm.animatedValue as Int
                        progressUpdateListener?.invoke(currentProgress)
                        updateRect(rectF = progressRectF)
                        ViewCompat.postInvalidateOnAnimation(this@FillProgressLayout)
                    }
                    doOnEnd {
                        doOnProgressEnd?.invoke(this@FillProgressLayout)
                        if (!isRestart) oldProgress = inputProgress
                    }
                    duration =
                        ((kotlin.math.abs(inputProgress - oldProgress)) * mDurationFactor).toLong()
                }
                mAnimator?.start()
            } else {
                currentProgress = inputProgress
                progressUpdateListener?.invoke(currentProgress)
                if (!isRestart) oldProgress = inputProgress
                updateRect(rectF = progressRectF)
                ViewCompat.postInvalidateOnAnimation(this)
                doOnProgressEnd?.invoke(this)
            }
        }
    }

    /**
     * This method is used to set progress animation interpolator
     * for different filling effects
     * @see TimeInterpolator
     * @param interpolator interpolator for animation
     * default value is
     * @see AccelerateDecelerateInterpolator
     */
    fun setAnimationInterpolator(interpolator: TimeInterpolator) {
        animInterpolator = interpolator
    }

    /**
     * This method is used to set the background color
     * @param resId color resource id
     */
    fun setProgressBackgroundColor(@ColorRes resId: Int) {
        if (isValidRes(resId)) {
            mBackgroundColor = ContextCompat.getColor(context, resId)
            initPaint()
        }
    }

    /**
     * This method is used to set the foreground/progress color
     * @param resId color resource id
     */
    fun setProgressColor(@ColorRes resId: Int) {
        if (isValidRes(resId)) {
            mProgressColor = ContextCompat.getColor(context, resId)
            initPaint()
        }
    }

    /**
     * This method is used to set multiple colors for gradient effect
     * @param resIds array of color resource ids
     * @param extractResColor flag for color extraction
     * @see ContextCompat.getColor
     */
    fun setProgressColors(@ColorRes resIds: IntArray, extractResColor: Boolean = true) {
        try {
            val filtered = resIds.filter { isValidRes(it) }
            mGradientColors = IntArray(filtered.size)
            filtered.forEachIndexed { index, i ->
                mGradientColors[index] =
                    if (extractResColor) ContextCompat.getColor(context, i) else i
            }
            initPaint()
        } catch (e: Exception) {
            log("Cannot use current color values!! Use integer array of R.color.colorName values")
        }
    }

    /**
     * This method is used to make progress layout corners rounded
     * and roundness is adjusted with a float value
     * @param radius corner radius for progress layout
     */
    fun setCornerRadius(radius: Float) {
        if (radius in 0f..maxProgress.toFloat()) {
            setRoundedCorners(true)
            this.mCornerRadius = radius
        }
    }

    /**
     * This method is used to make progress layout corners rounded
     * @param isRounded flag for rounded corners support
     */
    fun setRoundedCorners(isRounded: Boolean) {
        this.isRounded = isRounded
    }

    /**
     * This method is used to adjust the movement of progress gradient
     * if true the gradient will move will progress and
     * if false the gradient will stay as background paint and will reveals with progress
     * @param gradMovement flag for gradient movement
     */
    fun setGradientMovement(gradMovement: Boolean) {
        this.gradientMovement = gradMovement
    }

    /**
     * @param duration should be in millis i.e 2000 for 2 seconds
     */
    fun setDuration(duration: Long) {
        if (duration.toInt() == 0 || duration < 0) return
        mDurationFactor = (duration / 100).toInt()
    }

    /**
     * This method is used to make progress restart or resume from old position
     * if true the gradient will restart from zero to progress value
     * if false the gradient will resume from old to new progress value
     * @param isRestart flag for gradient movement
     */
    fun shouldStartFromZero(isRestart: Boolean) {
        this.isRestart = isRestart
    }

    /**
     * This method is used to set the direction of fill
     * from values LEFT_TO_RIGHT to BOTTOM_TO_TOP
     * default value is #LEFT_TO_RIGHT
     * @param direction value for fill direction
     * @see LEFT_TO_RIGHT
     */
    fun setFillDirection(direction: Int) {
        mDirection = if (direction in LEFT_TO_RIGHT..BOTTOM_TO_TOP) direction else defDirection
    }

    /**
     * This method is used to set the direction of gradient fill
     * from values LEFT_TO_RIGHT to BOTTOM_LEFT_TO_TOP_RIGHT
     * default value is #LEFT_TO_RIGHT
     * @param direction value for fill direction
     * @see LEFT_TO_RIGHT
     */
    fun setGradientDirection(direction: Int) {
        mGradientDirection =
            if (direction in LEFT_TO_RIGHT..BOTTOM_LEFT_TO_TOP_RIGHT) direction else defDirection
    }

    /**
     * This method is used to set a listener to get callback
     * when progress animation ends
     * @param listener a lambda function to invoke after progress animation ends
     */
    fun setDoOnProgressEnd(listener: ((v: View) -> Unit)) {
        doOnProgressEnd = listener
    }

    /**
     * This method is used to set a listener to get callback
     * for progress updates
     * @param listener a lambda function to invoke when progress changes
     * in both animated and non-animated case
     */
    fun setProgressUpdateListener(listener: ((progress: Int) -> Unit)) {
        progressUpdateListener = listener
    }

    private fun getGradientRect(progressRect: RectF): RectF {
        val outRect = RectF(progressRect)
        when (mGradientDirection) {
            LEFT_TO_RIGHT -> {
                outRect.left = progressRect.left
                outRect.top = progressRect.centerY()
                outRect.right = progressRect.right
                outRect.bottom = progressRect.centerY()
            }
            TOP_TO_BOTTOM -> {
                outRect.left = progressRect.centerX()
                outRect.top = progressRect.top
                outRect.right = progressRect.centerX()
                outRect.bottom = progressRect.bottom
            }
            RIGHT_TO_LEFT -> {
                outRect.left = progressRect.right
                outRect.top = progressRect.centerY()
                outRect.right = progressRect.left
                outRect.bottom = progressRect.centerY()
            }
            BOTTOM_TO_TOP -> {
                outRect.left = progressRect.centerX()
                outRect.top = progressRect.bottom
                outRect.right = progressRect.centerX()
                outRect.bottom = progressRect.top
            }
            TOP_LEFT_TO_BOTTOM_RIGHT -> {
                outRect.left = progressRect.left
                outRect.top = progressRect.top
                outRect.right = progressRect.right
                outRect.bottom = progressRect.bottom
            }
            TOP_RIGHT_TO_BOTTOM_LEFT -> {
                outRect.left = progressRect.right
                outRect.top = progressRect.top
                outRect.right = progressRect.left
                outRect.bottom = progressRect.bottom
            }
            BOTTOM_RIGHT_TO_TOP_LEFT -> {
                outRect.left = progressRect.right
                outRect.top = progressRect.bottom
                outRect.right = progressRect.left
                outRect.bottom = progressRect.top
            }
            BOTTOM_LEFT_TO_TOP_RIGHT -> {
                outRect.left = progressRect.left
                outRect.top = progressRect.bottom
                outRect.right = progressRect.right
                outRect.bottom = progressRect.top
            }
        }
        return outRect
    }

    //--------------------------------------------------------------------- --------------------------------------------//
    // default background setters overridden as we have our own background color implementation [#backRectF]
    override fun setBackground(background: Drawable?) {}

    override fun setBackgroundColor(color: Int) {}

    override fun setBackgroundResource(resid: Int) {}

    override fun setBackgroundDrawable(background: Drawable?) {}
    //-----------------------------------------------------------------------------------------------------------------//

    private fun log(logValue: String) {
        if (BuildConfig.DEBUG)
            Log.e(FillProgressLayout::class.java.simpleName, logValue)
    }

}