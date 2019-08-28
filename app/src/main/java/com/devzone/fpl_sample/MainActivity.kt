package com.devzone.fpl_sample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var isFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    fun toggleFill(view: View) {
        val button: AppCompatButton = view as AppCompatButton
        button.isEnabled = false

        isFilled = !isFilled
        fillL.setProgress(if (isFilled) 100 else 0)
        fillL.setDoOnProgressEnd { v ->
            button.isEnabled = true;button.text = if (isFilled) "Unfill" else "Fill"
        }

    }
}
