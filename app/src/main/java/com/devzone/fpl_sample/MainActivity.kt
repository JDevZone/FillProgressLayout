package com.devzone.fpl_sample

import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        handlePreciseControls()
        fillNF.setAnimationInterpolator(LinearInterpolator())


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

        if (isReverse) {
            val reverseProgress = kotlin.math.abs(100 - progress)
            fillB.setProgress(reverseProgress, isAnimated)
        } else
            fillB.setProgress(progress, isAnimated)
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
}
