package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.widget.databinding.ActivityMainBinding
import com.example.widget.databinding.ConfigLayoutBinding
import com.example.widget.databinding.WidgetConfigureBinding

class MainActivity: AppCompatActivity() {
    private val data = Data()
    private lateinit var bnd: WidgetConfigureBinding
    private lateinit var mergeBnd: ConfigLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bnd = WidgetConfigureBinding.inflate(layoutInflater)
        mergeBnd = ConfigLayoutBinding.bind(bnd.root)
        setContentView(bnd.root)

        bnd.addButton.setOnClickListener {
            setColors()
            val updateIntent = Intent(this, WidgetProvider::class.java).apply {
                action = ACTION_MANUAL_UPDATE
            }
            sendBroadcast(updateIntent)
        }
    }

    private fun setColors()
    {
        val bgAlpha = mergeBnd.bgAlpha.text.toString()
        val bgRed = mergeBnd.bgRed.text.toString()
        val bgGreen = mergeBnd.bgGreen.text.toString()
        val bgBlue = mergeBnd.bgBlue.text.toString()
        val textAlpha = mergeBnd.textAlpha.text.toString()
        val textRed = mergeBnd.textRed.text.toString()
        val textGreen = mergeBnd.textGreen.text.toString()
        val textBlue = mergeBnd.textBlue.text.toString()

        if (bgAlpha != "" && bgRed != "" && bgGreen != "" && bgBlue != "")
        {
            val backgroundArgb = ("$bgAlpha,$bgRed,$bgGreen,$bgBlue")
            data.save(this, "background_argb", backgroundArgb)
        }

        if (textAlpha != "" && textRed != "" && textGreen != "" && textBlue != "")
        {
            val textArgb = ("$textAlpha,$textRed,$textGreen,$textBlue")
            data.save(this, "text_argb", textArgb)
        }
    }
}