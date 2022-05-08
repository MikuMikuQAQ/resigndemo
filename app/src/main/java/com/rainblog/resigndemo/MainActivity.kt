package com.rainblog.resigndemo

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.view.marginTop
import com.rainblog.resigndemo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var layout: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layout = ActivityMainBinding.inflate(this.layoutInflater)
        setContentView(layout.root)

        layout.button.setOnClickListener {
            Toast.makeText(this, getString(R.string.thank), Toast.LENGTH_SHORT).show()
            finish()
        }
        layout.button2.setOnClickListener {
            it.visibility = View.GONE
            layout.resignView.visibility = View.VISIBLE
        }
    }
}