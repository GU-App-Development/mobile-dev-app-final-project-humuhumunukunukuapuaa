package com.zybooks.reeftriggerfish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

class PlayActivity : AppCompatActivity() {

    private lateinit var playButton : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val action = supportActionBar
        val bool = false
        action?.hide()
        playButton = findViewById(R.id.playBtn)
        playButton.setOnClickListener {
            startActivity(Intent(
                this@PlayActivity,
                MainActivity::class.java))
        }
    }
}