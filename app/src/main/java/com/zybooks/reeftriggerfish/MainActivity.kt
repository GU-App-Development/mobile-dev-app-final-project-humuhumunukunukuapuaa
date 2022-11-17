package com.zybooks.reeftriggerfish

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import com.zybooks.reeftriggerfish.uitel.OnSwipeListener
import kotlin.math.abs
import kotlin.math.floor

private const val LIFE_CYCLE = "LifeCycle"

class MainActivity : AppCompatActivity()
{
    private lateinit var cell : ArrayList<ImageView>
    private lateinit var scoreResult : TextView
    private lateinit var scoreTargetView: TextView
    private lateinit var movesLeftView: TextView
    private lateinit var mouseHandler: Handler

    private var cellWidth : Int = 0
    private var screenWidth : Int = 0
    private var screenHeight : Int = 0
    private var score : Int = 0

    //TODO: Edit game view to display winCon criteria
    private var scoreTarget : Int = 0
    private var movesLeft : Int = 0
    private var totalMoves: Int = 0
    private var winCon : String = ""
    private var isGameOver : Boolean = false
    private var didUserWin : Boolean = false

    private var gameState: String = ""

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

    // *************** OBJECT AND BOARD CREATION ***************
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(LIFE_CYCLE, "onCreate")

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        cellWidth = screenWidth / numCells
        cell = ArrayList()

        if (savedInstanceState != null) {
            Log.d(LIFE_CYCLE, "loading existing instance...")
            gameState = savedInstanceState.getString("gameState")!!
            movesLeft = savedInstanceState.getInt("movesLeft")
            score = savedInstanceState.getInt("score")

            scoreTarget = 50
            totalMoves = 10
            createBoard(gameState)
        } else {
            scoreTarget = 50
            movesLeft = 10
            totalMoves = 10
            score = 0
            createBoard()
        }

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

        // TODO: Manipulate 'winCon' to adjust game for various win conditions
        winCon = ""

        // Display Values (Score, Target Score, Moves Left)
        scoreResult = findViewById(R.id.score_view)
        scoreResult.text = getString(R.string.score_display, score)

        scoreTargetView = findViewById(R.id.score_target_view)
        scoreTargetView.text = getString(R.string.score_target, scoreTarget)

        movesLeftView = findViewById(R.id.moves_left_view)
        movesLeftView.text = getString(R.string.moves_left, movesLeft)

        mouseHandler = Handler()
        startLoop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(LIFE_CYCLE, "saving instance...")

        // stringify gameboard
        var boardString = StringBuilder()
        for(i in 0 until numCells * numCells) {
            val value = when(cell[i].tag as Int) {
                R.drawable.bluecandy -> 0
                R.drawable.greencandy -> 1
                R.drawable.orangecandy -> 2
                R.drawable.redcandy -> 3
                R.drawable.yellowcandy -> 4
                R.drawable.purplecandy -> 5
                else -> R.drawable.transparent
            }
            boardString.append(value.toString())
        }

        outState.putString("gameState", boardString.toString())
        outState.putInt("movesLeft", movesLeft)
        outState.putInt("score", score)
    }

    private fun createBoard(gameState: String = ""){
        val gridLayout = findViewById<GridLayout>(R.id.gameBoard)
        gridLayout.rowCount = numCells
        gridLayout.columnCount = numCells
        gridLayout.layoutParams.width = screenWidth
        gridLayout.layoutParams.height = screenHeight

        for (i in 0 until numCells * numCells) {
            val imageView = ImageView(this)
            imageView.id = i
            imageView.layoutParams = android.view.ViewGroup.LayoutParams(cellWidth, cellWidth)
            imageView.maxHeight = cellWidth
            imageView.maxWidth = cellWidth

            if (gameState.isNotEmpty()) {
                imageView.setImageResource(cellImages[gameState[i].digitToInt()])
                imageView.tag = cellImages[gameState[i].digitToInt()]
            }
            else {
                val random : Int = floor(Math.random() * cellImages.size).toInt()
                imageView.setImageResource(cellImages[random])
                imageView.tag = cellImages[random]
            }

            cell.add(imageView)
            gridLayout.addView(imageView)
        }
    }

    // *************** CORE GAME FUNCTIONS ***************
    private val loopChecker : Runnable = object : Runnable {
        override fun run() {
            try {
                if(!isGameOver){
                    checkIfGameOver()

                    //checkBoardForPlusPattern()

                    checkRowForFive()
                    checkColumnForFive()
                    checkRowForFour()
                    checkColumnForFour()
                    checkRowForThree()
                    checkColumnForThree()

                    moveDownCells()
                } else {
                    clearBoard()
                    showGameResult()
                }
            }
            finally {
                mouseHandler.postDelayed(this, interval)
            }
        }
    }

    private fun startLoop(){loopChecker.run()}

    fun swapCells(){
        Log.d("cell1:", cellToBeDragged.toString())
        Log.d("cell2:", cellToBeReplaced.toString())

        val background1 : Int = cell[cellToBeReplaced].tag as Int
        val background2 : Int = cell[cellToBeDragged].tag as Int

        cell[cellToBeDragged].setImageResource(background1)
        cell[cellToBeReplaced].setImageResource(background2)

        cell[cellToBeDragged].tag = background1
        cell[cellToBeReplaced].tag = background2

        if (!checkValidMove(cellToBeDragged, cellToBeReplaced)) {
            // unswap if not valid move
            cell[cellToBeDragged].tag = background2
            cell[cellToBeDragged].setImageResource(background2)
            cell[cellToBeReplaced].tag = background1
            cell[cellToBeReplaced].setImageResource(background1)
        }
        else {
            movesLeft--
            movesLeftView.text = getString(R.string.moves_left, movesLeft)
        }
    }

    // FIXME: I think this implementation is buggy
    private fun moveDownCells(){
        val firstRow = arrayOf(0,1,2,3,4,5,6,7)
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

    private fun checkIfGameOver(){
        when (winCon) {
            "moves" -> {
                if(movesLeft <= 0){
                    didUserWin = true
                    isGameOver = true
                }
            }
            "score" -> {
                if(score >= scoreTarget){
                    didUserWin = true
                    isGameOver = true
                }
                if(score < scoreTarget && movesLeft <= 0){
                    didUserWin = false
                    isGameOver = true
                }
            }
            else -> {
                // Possible change: let game continue until user says they want to be done?

                // Currently combines score and moves (i.e. "get score >= 40 in 15 or less moves")
                if(score >= scoreTarget && movesLeft >= 0){
                    didUserWin = true
                    isGameOver = true
                }
                if(movesLeft <= 0){
                    didUserWin = false
                    isGameOver = true
                }
            }
        }
    }

    private fun clearBoard(){
        val gridLayout = findViewById<GridLayout>(R.id.gameBoard)
        gridLayout.rowCount = numCells
        gridLayout.columnCount = numCells
        gridLayout.layoutParams.width = screenWidth
        gridLayout.layoutParams.height = screenHeight

        for(i in 0 until numCells * numCells){
            cell[i].setImageResource(emptyCell)
            cell[i].tag = emptyCell
            gridLayout.getChildAt(i).tag = emptyCell
        }
    }

    private fun resetBoard() {
        clearBoard()

        for(i in 0 until numCells * numCells){
            if(cell[i].tag as Int == emptyCell){
                val randomColor : Int = abs(Math.random() * cellImages.size).toInt()
                cell[i].setImageResource(cellImages[randomColor])
                cell[i].tag = cellImages[randomColor]
            }
        }
    }

    private fun showGameResult() {
        // hide game board
        val gridLayout = findViewById<GridLayout>(R.id.gameBoard)
        gridLayout.isVisible = false

        val gameResultLayout = findViewById<LinearLayout>(R.id.game_result_layout)
        gameResultLayout.isVisible = true
        val resultMessageView = findViewById<TextView>(R.id.result_message_view)

        // Display feedback to user
        // TODO: make end screen look nicer :)
        if(didUserWin){
            resultMessageView.text = getString(R.string.win_game_message)
        } else {
            resultMessageView.text = getString(R.string.lose_game_message)
        }

        // New Game Button
        val newGameButton = findViewById<Button>(R.id.new_game_button)
        newGameButton.setOnClickListener(this::onNewGameButtonClick)
    }

    private fun onNewGameButtonClick(view: View) {
        Log.d("NewGame", "onNewGameButtonClick")
        val gameResultLayout = findViewById<LinearLayout>(R.id.game_result_layout)
        gameResultLayout.isVisible = false

        val gridLayout = findViewById<GridLayout>(R.id.gameBoard)
        gridLayout.isVisible = true

        // reset game vars
        winCon = ""
        scoreTarget = 50
        movesLeft = 10
        score = 0
        didUserWin = false
        isGameOver = false

        // update display values
        scoreResult.text = getString(R.string.score_display, score)
//        scoreTargetView.text = getString(R.string.score_target, scoreTarget)
        movesLeftView.text = getString(R.string.moves_left, movesLeft)

        resetBoard()
        startLoop()
    }

    // *************** SCORE CHECKING ***************

    private fun checkValidMove(cellToBeDragged: Int, cellToBeReplaced: Int): Boolean {
        val validMove = (checkRowForFive(true) || checkColumnForFive(true) ||
                checkRowForFour(true) ||checkColumnForFour(true) ||
                checkRowForThree(true) || checkColumnForThree(true))

        Log.d("isValidMove", validMove.toString())

        return validMove
    }

    //  ROWS
    private fun checkRowForThree(check: Boolean = false): Boolean {
        for( i in 0..61){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell
            // don't check last 2 columns
            val notValid = arrayOf(6,7,14,15,22,23,30,31,38,39,46,47,54,55,62,63)
            val list = listOf(*notValid)

            if(!list.contains(i)){

                if(cell[i].tag as Int == chosenCandy
                    && !isBlank
                    && cell[i + 1].tag as Int == chosenCandy
                    && cell[i + 2].tag as Int == chosenCandy
                ){
                    // if checking for valid return true
                    if (check) {
                        return true
                    }
                    // update score
                    if (movesLeft < totalMoves) {
                        score += 3
                        scoreResult.text = getString(R.string.score_display, score)
                    }

                    // remove cells
                    cell[i].setImageResource(emptyCell)
                    cell[i].tag = emptyCell

                    cell[i + 1].setImageResource(emptyCell)
                    cell[i + 1].tag = emptyCell

                    cell[i + 2].setImageResource(emptyCell)
                    cell[i + 2].tag = emptyCell
                    moveDownCells()
                }
            }
        }
        return false
    }

    private fun checkRowForFour(check: Boolean = false): Boolean {
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
                    // if checking for valid return true
                    if (check) {
                        return true
                    }
                    // update score
                    if (movesLeft < totalMoves) {
                        score += 4
                        scoreResult.text = getString(R.string.score_display, score)
                    }

                    // remove cells
                    cell[i].setImageResource(emptyCell)
                    cell[i].tag = emptyCell

                    cell[i + 1].setImageResource(emptyCell)
                    cell[i + 1].tag = emptyCell

                    cell[i + 2].setImageResource(emptyCell)
                    cell[i + 2].tag = emptyCell

                    cell[i + 3].setImageResource(emptyCell)
                    cell[i + 3].tag = emptyCell

                    moveDownCells()
                }
            }
        }
        return false
    }

    private fun checkRowForFive(check: Boolean = false): Boolean{
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
                    // if checking for valid return true
                    if (check) {
                        return true
                    }
                    // update score
                    if (movesLeft < totalMoves) {
                        score += 5
                        scoreResult.text = getString(R.string.score_display, score)
                    }

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

                    moveDownCells()
                }
            }
        }
        return false
    }

    // COLUMNS
    private fun checkColumnForThree(check: Boolean = false): Boolean {
        for( i in 0..47){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell

            if(cell[i].tag as Int == chosenCandy
                && !isBlank
                && cell[i + numCells].tag as Int == chosenCandy
                && cell[i + 2 * numCells].tag as Int == chosenCandy
            ){
                // if checking for valid return true
                if (check) {
                    return true
                }

                // update score
                if (movesLeft < totalMoves) {
                    score += 3
                    scoreResult.text = getString(R.string.score_display, score)
                }

                // 'remove' the cells
                cell[i].setImageResource(emptyCell)
                cell[i].tag = emptyCell

                cell[i + numCells].setImageResource(emptyCell)
                cell[i + numCells].tag = emptyCell

                cell[i + 2 * numCells].setImageResource(emptyCell)
                cell[i + 2 * numCells].tag = emptyCell
                moveDownCells()
            }
        }
        return false
    }

    private fun checkColumnForFour(check: Boolean = false): Boolean{
        for( i in 0..40){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell

            if(cell[i].tag as Int == chosenCandy
                && !isBlank
                && cell[i + numCells].tag as Int == chosenCandy
                && cell[i + 2 * numCells].tag as Int == chosenCandy
                && cell[i + 3 * numCells].tag as Int == chosenCandy
            ){
                // if checking for valid return true
                if (check) {
                    return true
                }

                // update score
                if (movesLeft < totalMoves) {
                    score += 4
                    scoreResult.text = getString(R.string.score_display, score)
                }

                // 'remove' the cells
                cell[i].setImageResource(emptyCell)
                cell[i].tag = emptyCell

                cell[i + numCells].setImageResource(emptyCell)
                cell[i + numCells].tag = emptyCell

                cell[i + 2 * numCells].setImageResource(emptyCell)
                cell[i + 2 * numCells].tag = emptyCell

                cell[i + 3 * numCells].setImageResource(emptyCell)
                cell[i + 3 * numCells].tag = emptyCell
                moveDownCells()
            }
        }
        return false
    }

    private fun checkColumnForFive(check: Boolean = false): Boolean{
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
                // if checking for valid return true
                if (check) {
                    return true
                }
                // update score
                if (movesLeft < totalMoves) {
                    score += 5
                    scoreResult.text = getString(R.string.score_display, score)
                }

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
                moveDownCells()
            }
        }
        return false
    }

    // SPECIAL PATTERNS
    private fun checkBoardForPlusPattern(){

    }

    // LIFE CYCLE
    override fun onStop() {
        super.onStop()
        Log.d(LIFE_CYCLE, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(LIFE_CYCLE, "onDestroy")
    }

    override fun onPause() {
        super.onPause()
        Log.d(LIFE_CYCLE, "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d(LIFE_CYCLE, "onResume")
    }
}
