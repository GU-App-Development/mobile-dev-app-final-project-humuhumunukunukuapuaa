package com.zybooks.reeftriggerfish

import android.content.Intent
import android.media.MediaPlayer
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
    private lateinit var music : MediaPlayer
    private lateinit var winCheer : MediaPlayer
    private lateinit var loseBoo : MediaPlayer
    private lateinit var matchSFX : MediaPlayer

    var numCells : Int = 8
    var cellToBeDragged : Int = 0
    var cellToBeReplaced : Int = 0
    private var cellWidth : Int = 0
    private var screenWidth : Int = 0
    private var screenHeight : Int = 0
    private var score : Int = 0
    private var scoreTarget : Int = 0
    private var movesLeft : Int = 0
    private var totalMoves: Int = 0
    private var winCon : String = ""
    private var gameState: String = ""
    private var isGameOver : Boolean = false
    private var didUserWin : Boolean = false
    var interval = 200L

    private var emptyCell : Int = R.drawable.transparent
    private var cellImages = intArrayOf(
        R.drawable.bluecandy,
        R.drawable.greencandy,
        R.drawable.orangecandy,
        R.drawable.redcandy,
        R.drawable.yellowcandy,
        R.drawable.purplecandy
    )


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
            scoreTarget = savedInstanceState.getInt("scoreTarget")
            totalMoves = savedInstanceState.getInt("totalMoves")
            createBoard(gameState)
        } else {
            getGameMode()
            movesLeft = totalMoves
            score = 0
            createBoard()
        }

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

        // Display Values (Score, Target Score, Moves Left)
        scoreResult = findViewById(R.id.score_view)
        scoreResult.text = getString(R.string.score_display, score)

        scoreTargetView = findViewById(R.id.score_target_view)
        scoreTargetView.text = getString(R.string.score_target, scoreTarget)

        movesLeftView = findViewById(R.id.moves_left_view)
        movesLeftView.text = getString(R.string.moves_left, movesLeft)

        mouseHandler = Handler()

        // load and start background music
        music = MediaPlayer.create(this, R.raw.stolen_music)
        music.setVolume(70.0f, 100.0f)
        music.start()

        // load SFX for when matches are made
        matchSFX = MediaPlayer.create(this, R.raw.ploing)

        // load SFX for when game ends
        winCheer = MediaPlayer.create(this, R.raw.cheer)
        loseBoo = MediaPlayer.create(this, R.raw.boooo)

        startLoop()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(LIFE_CYCLE, "saving instance...")

        // stringify game-board
        val boardString = StringBuilder()
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
        outState.putInt("scoreTarget", scoreTarget)
        outState.putInt("totalMoves", totalMoves)
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


    /*
        **********  CORE GAME FUNCTIONS  **********
    */


    private fun getGameMode() {
        val intent = intent.extras
        when (intent?.getInt(SET_DIFFICULTY)) {
            0 -> {scoreTarget = 45 // EASY
                  totalMoves = 15}
            1 -> {scoreTarget = 50 // NORMAL
                totalMoves = 10}
            2 -> {scoreTarget = 60 // HARD
                totalMoves = 7}
        }
    }

    private fun startLoop(){loopChecker.run()}
    private val loopChecker : Runnable = object : Runnable {
        override fun run() {
            try {
                if(!isGameOver){
                    //checkBoardForPlusPattern()
                    checkBoardForSquarePattern()

                    checkRowForFive()
                    checkColumnForFive()
                    checkRowForFour()
                    checkColumnForFour()
                    checkRowForThree()
                    checkColumnForThree()

                    moveDownCells()
                    checkIfGameOver()
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
            // un-swap if not valid move
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

    private fun showGameResult() {

        // hide game board
        val gridLayout = findViewById<GridLayout>(R.id.gameBoard)
        gridLayout.isVisible = false

        // Reveal game-end screen
        val gameResultLayout = findViewById<LinearLayout>(R.id.game_result_layout)
        val resultMessageView = findViewById<TextView>(R.id.result_message_view)
        gameResultLayout.isVisible = true

        // Display feedback to user
        if(didUserWin){
            resultMessageView.text = getString(R.string.win_game_message)
            winCheer.start()
        } else {
            resultMessageView.text = getString(R.string.lose_game_message)
            loseBoo.start()
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

        // Direct back to main screen
        startActivity(Intent(
            this@MainActivity,
            PlayActivity::class.java))

        music.stop()
        winCheer.stop()
        loseBoo.stop()
    }

    /*
        **********  PATTERN CHECKING  **********
    */

    private fun checkValidMove(cellToBeDragged: Int, cellToBeReplaced: Int): Boolean {
        val validMove = (checkRowForFive(true) || checkColumnForFive(true) ||
                checkRowForFour(true) ||checkColumnForFour(true) ||
                checkRowForThree(true) || checkColumnForThree(true) ||
                checkBoardForSquarePattern(true))

        Log.d("isValidMove", validMove.toString())

        if(validMove)
            matchSFX.start()

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

        //  COLUMNS

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


        //  SPECIAL PATTERNS

    private fun checkBoardForPlusPattern(){

    }

    private fun checkBoardForSquarePattern (check: Boolean = false): Boolean {
        for( i in 0..61){
            val chosenCandy = cell[i].tag
            val isBlank : Boolean = cell[i].tag == emptyCell
            // don't check last columns & bottom row
            val notValid = arrayOf(7,15,23,31,39,47,55,56,57,58,59,60,61,62,63)
            val list = listOf(*notValid)

            if(!list.contains(i)){

                if(cell[i].tag as Int == chosenCandy
                    && !isBlank
                    && cell[i + 1].tag as Int == chosenCandy
                    && cell[i + numCells].tag as Int == chosenCandy
                    && cell[i + numCells + 1].tag as Int == chosenCandy
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

                    cell[i + numCells].setImageResource(emptyCell)
                    cell[i + numCells].tag = emptyCell

                    cell[i + numCells + 1].setImageResource(emptyCell)
                    cell[i + numCells + 1].tag = emptyCell

                    moveDownCells()
                }
            }
        }
        return false
    }


    /*
        **********  LIFE CYCLE  **********
    */


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
