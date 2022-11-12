package com.zybooks.reeftriggerfish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.TextView
import com.zybooks.reeftriggerfish.uitel.OnSwipeListener

class MainActivity : AppCompatActivity() {

    // Adding candies
    var candyImages = intArrayOf(
        R.drawable.bluecandy,
        R.drawable.greencandy,
        R.drawable.orangecandy,
        R.drawable.redcandy,
        R.drawable.yellowcandy,
        R.drawable.purplecandy
    )

    var blockWidth : Int = 0
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

        scoreResult = findViewById(R.id.score)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels

        var screenHeight = displayMetrics.heightPixels

        blockWidth = screenWidth / numBlocks

        candy = ArrayList()
        createBoard()

        for (imageView in candy){
            imageView.setOnTouchListener(object : OnSwipeListener(this){
                override fun onSwipeRight() {
                    super.onSwipeRight()
                    candyToBeDragged = imageView.id
                    candyToBeReplaced = candyToBeDragged + 1
                    candySwap()
                }
                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    candyToBeDragged = imageView.id
                    candyToBeReplaced = candyToBeDragged - 1
                    candySwap()
                }
                override fun onSwipeTop() {
                    super.onSwipeTop()
                    candyToBeDragged = imageView.id
                    candyToBeReplaced = candyToBeDragged - numBlocks
                    candySwap()
                }
                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    candyToBeDragged = imageView.id
                    candyToBeReplaced = candyToBeDragged + numBlocks
                    candySwap()
                }
            })
        }

        mouseHandler = Handler()
        startLoop()

    }

    fun startLoop(){

    }

    fun createBoard(){

    }

    fun candySwap(){
        var background1 : Int = candy.get(candyToBeReplaced).tag as Int
        var background2 : Int = candy.get(candyToBeDragged).tag as Int

        candy.get(candyToBeDragged).setImageResource(background1)
        candy.get(candyToBeReplaced).setImageResource(background2)

        candy.get(candyToBeDragged).setTag(background1)
        candy.get(candyToBeReplaced).setTag(background2)
    }
}