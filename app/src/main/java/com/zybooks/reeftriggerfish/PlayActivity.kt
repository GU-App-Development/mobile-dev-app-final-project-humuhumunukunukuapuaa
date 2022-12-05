package com.zybooks.reeftriggerfish

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView

private var difficulty = "normal"
const val SET_DIFFICULTY = "SET_DIFFICULTY"
private const val EASY = 0
private const val NORMAL = 1
private const val HARD = 2

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
            val intent = Intent(
                this@PlayActivity,
                MainActivity::class.java)
            intent.putExtra(SET_DIFFICULTY, EASY)
            startActivity(intent)
        }
        playButtonNormal.setOnClickListener {
            val intent = Intent(
                this@PlayActivity,
                MainActivity::class.java)
            intent.putExtra(SET_DIFFICULTY, NORMAL)
            startActivity(intent)
        }
        playButtonHard.setOnClickListener {
            val intent = Intent(
                this@PlayActivity,
                MainActivity::class.java)
            intent.putExtra(SET_DIFFICULTY, HARD)
            startActivity(intent)
        }
    }

    fun checkDifficulty() : String {
        return difficulty
    }
}