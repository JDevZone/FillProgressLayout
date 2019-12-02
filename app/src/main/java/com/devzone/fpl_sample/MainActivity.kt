package com.devzone.fpl_sample

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun toggleFill(view: View) {
        val button: Button = view as Button
        button.isEnabled = false

        isFilled = !isFilled
        fillL.setProgress(if (isFilled) 100 else 0)
        fillB.setProgress(if (isFilled) 100 else 0)
        fillL.setProgressColors(intArrayOf(R.color.colorGradient1,R.color.colorGradient2))
        fillB.setDoOnProgressEnd { v ->
            button.isEnabled = true;button.text = if (isFilled) "Unfill" else "Fill"
        }

    }
}
