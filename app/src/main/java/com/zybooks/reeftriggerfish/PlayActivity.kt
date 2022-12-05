package com.zybooks.reeftriggerfish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

private var difficulty = "normal"

class PlayActivity : AppCompatActivity() {

    private lateinit var playButtonEasy : ImageView
    private lateinit var playButtonNormal : ImageView
    private lateinit var playButtonHard : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        val action = supportActionBar
        action?.hide()
        playButtonEasy = findViewById(R.id.playBtnEasy)
        playButtonNormal = findViewById(R.id.playBtnNormal)
        playButtonHard = findViewById(R.id.playBtnHard)

        playButtonEasy.setOnClickListener {
            startActivity(Intent(
                this@PlayActivity,
                MainActivity::class.java))
        }
        playButtonNormal.setOnClickListener {
            startActivity(Intent(
                this@PlayActivity,
                MainActivity::class.java))
        }
        playButtonHard.setOnClickListener {
            startActivity(Intent(
                this@PlayActivity,
                MainActivity::class.java))
        }
    }

    fun checkDifficulty() : String {
        return difficulty
    }
}