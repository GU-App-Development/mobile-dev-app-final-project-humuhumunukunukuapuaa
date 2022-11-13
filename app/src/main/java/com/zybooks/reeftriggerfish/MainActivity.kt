package com.zybooks.reeftriggerfish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import com.zybooks.reeftriggerfish.uitel.OnSwipeListener
import java.util.Arrays.asList
import kotlin.math.abs
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    private lateinit var cell : ArrayList<ImageView>
    private lateinit var scoreResult : TextView
    private lateinit var mouseHandler: Handler

    private var cellWidth : Int = 0
    private var screenWidth : Int = 0
    private var screenHeight : Int = 0
    private var score : Int = 0
    private var emptyCell : Int = R.drawable.transparent
    private var cellImages = intArrayOf(
        R.drawable.bluecandy,
        R.drawable.greencandy,
        R.drawable.orangecandy,
        R.drawable.redcandy,
        R.drawable.yellowcandy,
        R.drawable.purplecandy
    )

    var interval = 200L
    var numCells : Int = 8
    var cellToBeDragged : Int = 0
    var cellToBeReplaced : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        scoreResult = findViewById(R.id.score)
        cellWidth = screenWidth / numCells
        cell = ArrayList()
        createBoard()

        // TODO: ADD CHECKS FOR VALID SWAPS
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

    private fun createBoard(){

        val gridLayout = findViewById<GridLayout>(R.id.gameBoard)
        gridLayout.rowCount = numCells
        gridLayout.columnCount = numCells
        gridLayout.layoutParams.width = screenWidth
        gridLayout.layoutParams.height = screenHeight

        for(i in 0 until numCells * numCells){

            val imageView = ImageView(this)
            imageView.id = i
            imageView.layoutParams = android.view.ViewGroup.LayoutParams(cellWidth, cellWidth)
            imageView.maxHeight = cellWidth
            imageView.maxWidth = cellWidth

            val random : Int = floor(Math.random() * cellImages.size).toInt()

            imageView.setImageResource(cellImages[random])
            imageView.tag = cellImages[random]

            cell.add(imageView)
            gridLayout.addView(imageView)
        }
    }

    fun swapCells(){

        val background1 : Int = cell[cellToBeReplaced].tag as Int
        val background2 : Int = cell[cellToBeDragged].tag as Int

        cell[cellToBeDragged].setImageResource(background1)
        cell[cellToBeReplaced].setImageResource(background2)

        cell[cellToBeDragged].tag = background1
        cell[cellToBeReplaced].tag = background2
    }

    // Todo: I think this implementation is buggy
    private fun moveDownCells(){

        val firstRow = arrayOf(1,2,3,4,5,6,7,8)
        val list = listOf(*firstRow)
        for (i in 55 downTo 0){
            if (cell[i + numCells].tag as Int == emptyCell){

                cell[i + numCells].setImageResource(cell[i].tag as Int)
                cell[i + numCells].tag = cell[i].tag as Int

                cell[i].setImageResource(emptyCell)
                cell[i].tag = emptyCell

                if(list.contains(i) && cell[i].tag == emptyCell){
                    val randomColor : Int = abs(Math.random() * cellImages.size).toInt()
                    cell[i].setImageResource(cellImages[randomColor])
                    cell[i].tag = cellImages[randomColor]
                }
            }
        }
        createNewCells()
    }

    private fun createNewCells(){

        for (i in 0..7){
            if(cell[i].tag as Int == emptyCell){
                val randomColor : Int = abs(Math.random() * cellImages.size).toInt()
                cell[i].setImageResource(cellImages[randomColor])
                cell[i].tag = cellImages[randomColor]
            }
        }
    }

    private val loopChecker : Runnable = object : Runnable {
        override fun run() {
            try {
                //checkBoardForPlusPattern()

                checkRowForFive()
                checkColumnForFive()
                checkRowForFour()
                checkColumnForFour()
                checkRowForThree()
                checkColumnForThree()

                moveDownCells()
            }
            finally {
                mouseHandler.postDelayed(this, interval)
            }
        }
    }
    private fun startLoop(){
        loopChecker.run()
    }

    // *************** SCORE CHECKING ***************

    //  ROWS
    private fun checkRowForThree()
    {
        for( i in 0..61){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell
            val notValid = arrayOf(6,7,14,15,22,23,30,31,38,39,46,47,54,55)
            val list = listOf(*notValid)

            if(!list.contains(i)){

                if(cell[i].tag as Int == chosenCandy
                    && !isBlank
                    && cell[i + 1].tag as Int == chosenCandy
                    && cell[i + 2].tag as Int == chosenCandy
                ){
                    // update score
                    score += 3
                    scoreResult.text = "$score"

                    // remove cells
                    cell[i].setImageResource(emptyCell)
                    cell[i].tag = emptyCell

                    cell[i + 1].setImageResource(emptyCell)
                    cell[i + 1].tag = emptyCell

                    cell[i + 2].setImageResource(emptyCell)
                    cell[i + 2].tag = emptyCell
                }
            }
        }
        moveDownCells()
    }

    private fun checkRowForFour(){
        for( i in 0..60){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell
            val notValid = arrayOf(5,6,7,13,14,15,21,22,23,29,30,31,37,38,39,45,46,47,53,54,55,61,62,63)
            val list = listOf(*notValid)

            if(!list.contains(i)){

                if(cell[i].tag as Int == chosenCandy
                    && !isBlank
                    && cell[i + 1].tag as Int == chosenCandy
                    && cell[i + 2].tag as Int == chosenCandy
                    && cell[i + 3].tag as Int == chosenCandy
                ){
                    // update score
                    score += 4
                    scoreResult.text = "$score"

                    // remove cells
                    cell[i].setImageResource(emptyCell)
                    cell[i].tag = emptyCell

                    cell[i + 1].setImageResource(emptyCell)
                    cell[i + 1].tag = emptyCell

                    cell[i + 2].setImageResource(emptyCell)
                    cell[i + 2].tag = emptyCell

                    cell[i + 3].setImageResource(emptyCell)
                    cell[i + 3].tag = emptyCell
                }
            }
        }
        moveDownCells()
    }

    private fun checkRowForFive(){
        for( i in 0..59){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell
            val notValid = arrayOf(4,5,6,7,12,13,14,15,20,21,22,23,28,29,30,31,36,37,38,39,44,45,46,47,52,53,54,55,60,61,62,63)
            val list = listOf(*notValid)

            if(!list.contains(i)){

                if(cell[i].tag as Int == chosenCandy
                    && !isBlank
                    && cell[i + 1].tag as Int == chosenCandy
                    && cell[i + 2].tag as Int == chosenCandy
                    && cell[i + 3].tag as Int == chosenCandy
                    && cell[i + 4].tag as Int == chosenCandy
                ){
                    // update score
                    score += 5
                    scoreResult.text = "$score"

                    // remove cells
                    cell[i].setImageResource(emptyCell)
                    cell[i].tag = emptyCell

                    cell[i + 1].setImageResource(emptyCell)
                    cell[i + 1].tag = emptyCell

                    cell[i + 2].setImageResource(emptyCell)
                    cell[i + 2].tag = emptyCell

                    cell[i + 3].setImageResource(emptyCell)
                    cell[i + 3].tag = emptyCell

                    cell[i + 4].setImageResource(emptyCell)
                    cell[i + 4].tag = emptyCell
                }
            }
        }
        moveDownCells()
    }

    // COLUMNS
    private fun checkColumnForThree()
    {
        for( i in 0..47){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell

            if(cell[i].tag as Int == chosenCandy
                && !isBlank
                && cell[i + numCells].tag as Int == chosenCandy
                && cell[i + 2 * numCells].tag as Int == chosenCandy
            ){
                // update score
                score += 3
                scoreResult.text = "$score"

                // 'remove' the cells
                cell[i].setImageResource(emptyCell)
                cell[i].tag = emptyCell

                cell[i + numCells].setImageResource(emptyCell)
                cell[i + numCells].tag = emptyCell

                cell[i + 2 * numCells].setImageResource(emptyCell)
                cell[i + 2 * numCells].tag = emptyCell
            }
        }
        moveDownCells()
    }

    private fun checkColumnForFour(){
        for( i in 0..40){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell

            if(cell[i].tag as Int == chosenCandy
                && !isBlank
                && cell[i + numCells].tag as Int == chosenCandy
                && cell[i + 2 * numCells].tag as Int == chosenCandy
                && cell[i + 3 * numCells].tag as Int == chosenCandy
            ){
                // update score
                score += 4
                scoreResult.text = "$score"

                // 'remove' the cells
                cell[i].setImageResource(emptyCell)
                cell[i].tag = emptyCell

                cell[i + numCells].setImageResource(emptyCell)
                cell[i + numCells].tag = emptyCell

                cell[i + 2 * numCells].setImageResource(emptyCell)
                cell[i + 2 * numCells].tag = emptyCell

                cell[i + 3 * numCells].setImageResource(emptyCell)
                cell[i + 3 * numCells].tag = emptyCell
            }
        }
        moveDownCells()
    }

    private fun checkColumnForFive(){
        for( i in 0..32){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell

            if(cell[i].tag as Int == chosenCandy
                && !isBlank
                && cell[i + numCells].tag as Int == chosenCandy
                && cell[i + 2 * numCells].tag as Int == chosenCandy
                && cell[i + 3 * numCells].tag as Int == chosenCandy
                && cell[i + 4 * numCells].tag as Int == chosenCandy
            ){
                // update score
                score += 5
                scoreResult.text = "$score"

                // 'remove' the cells
                cell[i].setImageResource(emptyCell)
                cell[i].tag = emptyCell

                cell[i + numCells].setImageResource(emptyCell)
                cell[i + numCells].tag = emptyCell

                cell[i + 2 * numCells].setImageResource(emptyCell)
                cell[i + 2 * numCells].tag = emptyCell

                cell[i + 3 * numCells].setImageResource(emptyCell)
                cell[i + 3 * numCells].tag = emptyCell

                cell[i + 4 * numCells].setImageResource(emptyCell)
                cell[i + 4 * numCells].tag = emptyCell
            }
        }
        moveDownCells()
    }

    // SPECIAL PATTERNS
    private fun checkBoardForPlusPattern(){

    }



}