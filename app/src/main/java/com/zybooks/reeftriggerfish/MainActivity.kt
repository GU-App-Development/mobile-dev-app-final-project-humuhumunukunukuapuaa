package com.zybooks.reeftriggerfish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.widget.ImageView
import android.widget.TextView
import com.zybooks.reeftriggerfish.uitel.OnSwipeListener
import java.util.Arrays.asList

class MainActivity : AppCompatActivity() {

    // Adding candies
    var cellImages = intArrayOf(
        R.drawable.bluecandy,
        R.drawable.greencandy,
        R.drawable.orangecandy,
        R.drawable.redcandy,
        R.drawable.yellowcandy,
        R.drawable.purplecandy
    )

    var cellWidth : Int = 0
    var numCells : Int = 8
    var screenWidth : Int = 0
    lateinit var cell : ArrayList<ImageView>

    var cellToBeDragged : Int = 0
    var cellToBeReplaced : Int = 0
    var emptyCell : Int = R.drawable.transparent

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

        cellWidth = screenWidth / numCells

        cell = ArrayList()
        createBoard()

        for (imageView in cell){
            imageView.setOnTouchListener(object : OnSwipeListener(this){
                override fun onSwipeRight() {
                    super.onSwipeRight()
                    cellToBeDragged = imageView.id
                    cellToBeReplaced = cellToBeDragged + 1
                    swapCells()
                }
                override fun onSwipeLeft() {
                    super.onSwipeLeft()
                    cellToBeDragged = imageView.id
                    cellToBeReplaced = cellToBeDragged - 1
                    swapCells()
                }
                override fun onSwipeTop() {
                    super.onSwipeTop()
                    cellToBeDragged = imageView.id
                    cellToBeReplaced = cellToBeDragged - numCells
                    swapCells()
                }
                override fun onSwipeBottom() {
                    super.onSwipeBottom()
                    cellToBeDragged = imageView.id
                    cellToBeReplaced = cellToBeDragged + numCells
                    swapCells()
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

    fun swapCells(){
        var background1 : Int = cell.get(cellToBeReplaced).tag as Int
        var background2 : Int = cell.get(cellToBeDragged).tag as Int

        cell.get(cellToBeDragged).setImageResource(background1)
        cell.get(cellToBeReplaced).setImageResource(background2)

        cell.get(cellToBeDragged).setTag(background1)
        cell.get(cellToBeReplaced).setTag(background2)
    }

    private fun moveDownCells(){
        val firstRow = arrayOf(1,2,3,4,5,6,7,8)
        val list = asList(*firstRow)
        for (i in 55 downTo 0){
            if (cell.get(i + numCells).tag as Int == emptyCell){

                cell.get(i + numCells).setImageResource(cell.get(i).tag as Int)
                cell.get(i + numCells).setTag(cell.get(i).tag as Int)

                cell.get(i).setImageResource(emptyCell)
                cell.get(i).setTag(emptyCell)
            }
        }
    }

    // *************** SCORE CHECKING ***************

    //  ROWS
    private fun checkRowForThree(){
        for( i in 0..61){
            var chosenCandy = cell.get(i).tag

            var isBlank : Boolean = cell.get(i).tag == emptyCell
            val notValid = arrayOf(6,7,14,15,22,23,30,31,38,39,46,47,54,55)

            val list = asList(*notValid)

            if(!list.contains(i)){
                var x = i

                if(cell.get(x++).tag as Int == chosenCandy
                    && !isBlank
                    && cell.get(x++).tag as Int == chosenCandy
                    && cell.get(x).tag as Int == chosenCandy
                ){
                    score += 3
                    scoreResult.text = "$score"
                    cell.get(x).setImageResource(emptyCell)
                    cell.get(x).setTag(emptyCell)
                    x--
                    cell.get(x).setImageResource(emptyCell)
                    cell.get(x).setTag(emptyCell)
                    x--
                    cell.get(x).setImageResource(emptyCell)
                    cell.get(x).setTag(emptyCell)
                }
            }

        }
        moveDownCells()
    }

    private fun checkRowForFour(){

    }

    private fun checkRowForFive(){

    }

    // COLUMNS
    private fun checkColumnForThree(){
        for( i in 0..47){
            var chosenCandy = cell.get(i).tag

            var isBlank : Boolean = cell.get(i).tag == emptyCell

            var x = i
            if(cell.get(x++).tag as Int == chosenCandy
                && !isBlank
                && cell.get(x+numCells).tag as Int == chosenCandy
                && cell.get(x+2*numCells).tag as Int == chosenCandy
            ){
                score += 3
                scoreResult.text = "$score"
                cell.get(x).setImageResource(emptyCell)
                cell.get(x).setTag(emptyCell)
                x += numCells
                cell.get(x).setImageResource(emptyCell)
                cell.get(x).setTag(emptyCell)
                x += numCells
                cell.get(x).setImageResource(emptyCell)
                cell.get(x).setTag(emptyCell)
            }
        }
        moveDownCells()
    }

    private fun checkColumnForFour(){

    }

    private fun checkColumnForFive(){

    }

    // SPECIAL PATTERNS



}