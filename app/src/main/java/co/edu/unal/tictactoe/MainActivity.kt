package co.edu.unal.tictactoe

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private var game: TicTacToeGame? = null
    private var boardView: BoardView? = null
    private var tvInfo: TextView? = null
    private var tvRoomId: TextView? = null
    private var tvP1Wins: TextView? = null
    private var tvP2Wins: TextView? = null
    private var tvP2Label: TextView? = null
    private var ivP2Icon: ImageView? = null

    private var isVsCpu: Boolean = true
    private var isGameActive: Boolean = true
    private var isHumanTurn: Boolean = true

    // Variables para el modo Multijugador Online
    private var isOnline: Boolean = false
    private var roomId: String? = null
    private var myRole: String = "X" // "X" (Host / Jugador 1) u "O" (Guest / Jugador 2)
    private var roomRef: DatabaseReference? = null
    private var roomEventListener: ValueEventListener? = null

    // Guardar quién iniciará la siguiente ronda
    private var nextStarter: Int = 1

    // Puntuación permanente
    private var p1Wins = 0
    private var p2Wins = 0

    private var prefs: SharedPreferences? = null
    private var humanMediaPlayer: MediaPlayer? = null
    private var computerMediaPlayer: MediaPlayer? = null
    private var gestureDetector: GestureDetector? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            prefs = getSharedPreferences("ttt_prefs", Context.MODE_PRIVATE)

            // Leer intents del Menú
            isOnline = intent?.getBooleanExtra("EXTRA_IS_ONLINE", false) ?: false
            roomId = intent?.getStringExtra("EXTRA_ROOM_ID")
            myRole = intent?.getStringExtra("EXTRA_MY_ROLE") ?: "X"
            isVsCpu = if (isOnline) false else (intent?.getBooleanExtra("EXTRA_VS_CPU", true) ?: true)
            val isNewGame = intent?.getBooleanExtra("EXTRA_NEW_GAME", true) ?: true

            game = TicTacToeGame()
            tvInfo = findViewById(R.id.tvInfo)
            tvRoomId = findViewById(R.id.tvRoomId)
            boardView = findViewById(R.id.boardView)
            tvP1Wins = findViewById(R.id.tvPlayer1Wins)
            tvP2Wins = findViewById(R.id.tvPlayer2Wins)
            tvP2Label = findViewById(R.id.tvPlayer2Label)
            ivP2Icon = findViewById(R.id.ivPlayer2Icon)

            val savedDifficulty = prefs?.getInt("mDifficulty", 0) ?: 0
            game?.setDifficultyLevel(savedDifficulty)

            // Configurar UI según la modalidad de juego
            if (isOnline) {
                tvRoomId?.visibility = View.VISIBLE
                tvRoomId?.text = "Sala: $roomId"
                ivP2Icon?.setImageResource(R.drawable.ic_bear_human2)
                tvP2Label?.text = "RIVAL"
                boardView?.setPlayerBitmaps(R.drawable.ic_bear_human, R.drawable.ic_bear_human2)
            } else if (isVsCpu) {
                tvRoomId?.visibility = View.GONE
                ivP2Icon?.setImageResource(R.drawable.ic_bear_robot)
                tvP2Label?.text = "CPU"
                boardView?.setPlayerBitmaps(R.drawable.ic_bear_human, R.drawable.ic_bear_robot)
            } else {
                tvRoomId?.visibility = View.GONE
                ivP2Icon?.setImageResource(R.drawable.ic_bear_human2)
                tvP2Label?.text = "JUGADOR 2"
                boardView?.setPlayerBitmaps(R.drawable.ic_bear_human, R.drawable.ic_bear_human2)
            }

            game?.let { g ->
                boardView?.setGame(g)
            }

            if (isNewGame && !isOnline) {
                p1Wins = 0
                p2Wins = 0
                saveScores()
            } else {
                p1Wins = prefs?.getInt("mHumanWins", 0) ?: 0
                p2Wins = prefs?.getInt("mComputerWins", 0) ?: 0
            }
            displayScores()

            boardView?.setOnTouchListener { view, event ->
                if (event.action == MotionEvent.ACTION_DOWN && isGameActive) {
                    val bView = boardView ?: return@setOnTouchListener false
                    val cellWidth = bView.getBoardCellWidth()
                    val cellHeight = bView.getBoardCellHeight()

                    if (cellWidth > 0 && cellHeight > 0) {
                        val col = event.x.toInt() / cellWidth
                        val row = event.y.toInt() / cellHeight
                        val pos = row * 3 + col

                        if (pos in 0 until TicTacToeGame.BOARD_SIZE) {
                            onCellClicked(pos)
                            view.performClick()
                        }
                    }
                }
                true
            }

            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmExitToMenu()
                }
            })

            gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    confirmExitToMenu()
                    return true
                }
            })

            val mainLayout = findViewById<View>(R.id.mainRootLayout)
            mainLayout?.setOnTouchListener { v, event ->
                gestureDetector?.onTouchEvent(event)
                if (event.action == MotionEvent.ACTION_DOWN) {
                    v.performClick()
                }
                false
            }

            if (isOnline) {
                setupOnlineGame()
            } else if (savedInstanceState == null) {
                startNewGame()
            }

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun setupOnlineGame() {
        val currentRoomId = roomId ?: return
        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(currentRoomId)

        roomEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val gameStatus = snapshot.child("gameStatus").value as? String ?: "waiting"
                val turn = snapshot.child("turn").value as? String ?: "X"
                val boardList = snapshot.child("board").value as? List<String>

                if (gameStatus == "waiting") {
                    tvInfo?.text = "Esperando rival..."
                    isGameActive = false
                    return
                }

                // Sincronizar el tablero desde Firebase
                boardList?.let { list ->
                    for (i in list.indices) {
                        val symbol = list[i]
                        val charValue = when (symbol) {
                            "X" -> TicTacToeGame.HUMAN_PLAYER
                            "O" -> TicTacToeGame.COMPUTER_PLAYER
                            else -> TicTacToeGame.OPEN_SPOT
                        }
                        game?.setMove(charValue, i)
                    }
                    boardView?.invalidate()
                }

                // Verificar si hay ganador
                val winner = game?.checkForWinner() ?: 0
                if (winner != 0) {
                    updateGameStatus(winner)
                    return
                }

                // Gestionar turno online
                isGameActive = true
                if (turn == myRole) {
                    isHumanTurn = true
                    tvInfo?.text = "¡Es tu turno! ($myRole)"
                } else {
                    isHumanTurn = false
                    tvInfo?.text = "Turno del rival... ⏳"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Error de conexión online", Toast.LENGTH_SHORT).show()
            }
        }

        roomRef?.addValueEventListener(roomEventListener as ValueEventListener)
    }

    private fun startNewGame() {
        game?.clearBoard()
        isGameActive = true

        if (isOnline) {
            val emptyBoard = listOf("", "", "", "", "", "", "", "", "")
            roomRef?.child("board")?.setValue(emptyBoard)
            roomRef?.child("turn")?.setValue("X")
            roomRef?.child("gameStatus")?.setValue("playing")
            return
        }

        isHumanTurn = (nextStarter == 1)
        boardView?.invalidate()

        if (isVsCpu) {
            if (isHumanTurn) {
                tvInfo?.text = "¡Es tu turno!"
            } else {
                tvInfo?.text = "Turno del Robot... 🤖"
                Handler(Looper.getMainLooper()).postDelayed({
                    if (isGameActive && !isFinishing) {
                        makeCpuMove()
                    }
                }, 800)
            }
        } else {
            tvInfo?.text = if (isHumanTurn) "Turno: Jugador 1 🐻" else "Turno: Jugador 2 🐼"
        }
    }

    private fun onCellClicked(location: Int) {
        val g = game ?: return
        if (!isGameActive) return

        if (isOnline) {
            if (isHumanTurn && g.getBoardState()[location] == TicTacToeGame.OPEN_SPOT) {
                val playerChar = if (myRole == "X") TicTacToeGame.HUMAN_PLAYER else TicTacToeGame.COMPUTER_PLAYER
                g.setMove(playerChar, location)
                playHumanSound()
                boardView?.invalidate()

                val currentBoard = g.getBoardState().map {
                    when (it) {
                        TicTacToeGame.HUMAN_PLAYER -> "X"
                        TicTacToeGame.COMPUTER_PLAYER -> "O"
                        else -> ""
                    }
                }
                val nextTurn = if (myRole == "X") "O" else "X"
                roomRef?.child("board")?.setValue(currentBoard)
                roomRef?.child("turn")?.setValue(nextTurn)
            }
            return
        }

        if (isVsCpu) {
            if (isHumanTurn) {
                if (g.setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
                    playHumanSound()
                    boardView?.invalidate()

                    val winner = g.checkForWinner()
                    if (winner == 0) {
                        isHumanTurn = false
                        tvInfo?.text = "Pensando... 🤖"

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isGameActive && !isFinishing) {
                                makeCpuMove()
                            }
                        }, 800)
                    } else {
                        updateGameStatus(winner)
                    }
                }
            }
        } else {
            val currentPlayer = if (isHumanTurn) TicTacToeGame.HUMAN_PLAYER else TicTacToeGame.COMPUTER_PLAYER
            if (g.setMove(currentPlayer, location)) {
                if (isHumanTurn) playHumanSound() else playComputerSound()
                boardView?.invalidate()

                val winner = g.checkForWinner()
                if (winner == 0) {
                    isHumanTurn = !isHumanTurn
                    tvInfo?.text = if (isHumanTurn) "Turno: Jugador 1 🐻" else "Turno: Jugador 2 🐼"
                } else {
                    updateGameStatus(winner)
                }
            }
        }
    }

    private fun makeCpuMove() {
        val g = game ?: return
        try {
            val move = g.getComputerMove()
            if (move != -1) {
                g.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                playComputerSound()
                boardView?.invalidate()

                val winnerAfterCpu = g.checkForWinner()
                if (winnerAfterCpu == 0) {
                    isHumanTurn = true
                    tvInfo?.text = "¡Es tu turno!"
                } else {
                    updateGameStatus(winnerAfterCpu)
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun updateGameStatus(winner: Int) {
        when (winner) {
            1 -> {
                tvInfo?.text = "¡Es un empate! 🤝"
                nextStarter = if (nextStarter == 1) 2 else 1
            }
            2 -> {
                if (isOnline) {
                    tvInfo?.text = if (myRole == "X") "¡Ganaste! 🎉" else "¡Ganó tu rival! 🐻"
                } else {
                    tvInfo?.text = if (isVsCpu) "¡Ganaste 🐻!" else "¡Ganó Jugador 1 🐻!"
                }
                p1Wins++
                nextStarter = 1
            }
            3 -> {
                if (isOnline) {
                    tvInfo?.text = if (myRole == "O") "¡Ganaste! 🎉" else "¡Ganó tu rival! 🐼"
                } else {
                    tvInfo?.text = if (isVsCpu) "¡Ganó el Robot 🤖!" else "¡Ganó Jugador 2 🐼!"
                }
                p2Wins++
                nextStarter = 2
            }
        }
        isGameActive = false
        displayScores()
        if (!isOnline) saveScores()

        Handler(Looper.getMainLooper()).postDelayed({
            if (!isFinishing) {
                startNewGame()
            }
        }, 2500)
    }

    private fun displayScores() {
        tvP1Wins?.text = "$p1Wins wins"
        tvP2Wins?.text = "$p2Wins wins"
    }

    private fun saveScores() {
        prefs?.edit()?.apply {
            putInt("mHumanWins", p1Wins)
            putInt("mComputerWins", p2Wins)
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        roomEventListener?.let {
            roomRef?.removeEventListener(it)
        }
    }

    private fun confirmExitToMenu() {
        try {
            AlertDialog.Builder(this)
                .setTitle("¿Salir al Menú Principal?")
                .setMessage("¿Estás seguro de que deseas salir?")
                .setPositiveButton("Sí") { _, _ -> finish() }
                .setNegativeButton("No", null)
                .show()
        } catch (e: Throwable) {
            finish()
        }
    }

    fun playHumanSound() {
        try {
            humanMediaPlayer?.start()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun playComputerSound() {
        try {
            computerMediaPlayer?.start()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            humanMediaPlayer = MediaPlayer.create(applicationContext, R.raw.sound_human)
            computerMediaPlayer = MediaPlayer.create(applicationContext, R.raw.sound_robot)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            humanMediaPlayer?.release()
            computerMediaPlayer?.release()
            humanMediaPlayer = null
            computerMediaPlayer = null
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}