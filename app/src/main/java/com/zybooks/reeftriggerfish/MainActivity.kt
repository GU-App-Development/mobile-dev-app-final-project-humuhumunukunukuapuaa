package com.zybooks.reeftriggerfish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    // Adding candies
    var candies = intArrayOf(
        R.drawable.bluecandy,
        R.drawable.greencandy,
        R.drawable.orangecandy,
        R.drawable.redcandy,
        R.drawable.yellowcandy,
        R.drawable.purplecandy
    )

    var widthofBlock : Int = 0
    var numBlocks : Int = 8
    var screenWidth : Int = 0
    lateinit var candy : ArrayList<ImageView>

    var candyToBeDragged : Int = 0
    var candyToBeReplaced : Int = 0
    var emptyBlock : Int = R.drawable.transparent

    lateinit var mouseHandler: Handler
    private lateinit var scoreResult : TextView
    var score : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}