package com.devzone.fpl_sample

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.view.animation.BounceInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.devzone.fillprogresslayout.FillProgressLayout
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handlePreciseControls()

        fillNF.setAnimationInterpolator(LinearInterpolator())


        fishCardFPL?.apply {
            setProgressUpdateListener { progress ->
                val color = if (progress > 60) R.color.colorBlue else R.color.colorRed
                fishIV?.colorFilter = PorterDuffColorFilter(
                    ContextCompat.getColor(this@MainActivity, color),
                    PorterDuff.Mode.SRC_IN
                )
            }
        }
        fishCardFPL.setAnimationInterpolator(OvershootInterpolator())
        fishCardFPL.setProgressBackgroundColor(Color.LTGRAY)
        fishCardFPL.setProgressColor(Color.parseColor("#00ffff"))

    }

    fun toggleFill(view: View) {
        val isAnimated = animateCB.isChecked
        val button: Button = view as Button
        button.isEnabled = false
        isFilled = !isFilled
        fillL?.setDoOnProgressEnd { _ ->
            button.isEnabled = true;button.text = if (isFilled) "UnFill" else "Fill"
        }
        fillL?.setProgress(if (isFilled) 100 else 0, isAnimated)
        fillNF?.setProgress(if (isFilled) 100 else 0)
        fillL?.setProgressColors(intArrayOf(R.color.colorGradient1, R.color.colorGradient2))
    }


    fun toggleFillLayout(view: View) {
        if(view !is FillProgressLayout) return
        val isAnimated = animateCB.isChecked
        view.apply {
            when (currentProgress){
                in 50..99,0 ->  setProgress(100,isAnimated)
                in 0..50,100 ->  setProgress(0,isAnimated)
            }
        }
    }

    private fun handlePreciseControls() {
        progressSeek?.apply {
            max = 100
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    updateLargeProgress(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {

                }

            })
        }

    }

    private fun updateLargeProgress(progress: Int) {
        val isAnimated = animateCB.isChecked
        val isReverse = reverseCB.isChecked
        val currentProgress = if (isReverse) kotlin.math.abs(100 - progress) else progress
        fillB.setProgress(currentProgress, isAnimated)
        fishCardFPL?.setProgress(currentProgress, isAnimated)
    }





}
