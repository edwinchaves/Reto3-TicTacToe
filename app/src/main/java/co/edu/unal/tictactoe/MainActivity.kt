package co.edu.unal.tictactoe

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var game: TicTacToeGame? = null
    private var boardView: BoardView? = null
    private var tvInfo: TextView? = null

    private var isVsCpu: Boolean = true
    private var isGameActive: Boolean = true
    private var isHumanTurn: Boolean = true

    private var humanMediaPlayer: MediaPlayer? = null
    private var computerMediaPlayer: MediaPlayer? = null
    private var gestureDetector: GestureDetector? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main)

            // Leer modo enviado desde MenuActivity
            isVsCpu = intent?.getBooleanExtra("EXTRA_VS_CPU", true) ?: true

            game = TicTacToeGame()
            tvInfo = findViewById(R.id.tvInfo)
            boardView = findViewById(R.id.boardView)

            game?.let { g ->
                boardView?.setGame(g)
            }

            // Escuchador de toques en el tablero
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

            // Botón atrás físico o gestual
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    confirmExitToMenu()
                }
            })

            // Gestos: doble toque fuera del tablero para volver al menú
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

            startNewGame()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun startNewGame() {
        game?.clearBoard()
        isGameActive = true
        isHumanTurn = true
        boardView?.invalidate()
        tvInfo?.text = if (isVsCpu) "¡Es tu turno!" else "Turno: Jugador 1 🐻"
    }

    private fun onCellClicked(location: Int) {
        val g = game ?: return
        if (!isGameActive) return

        if (isVsCpu) {
            // --- MODO VS ROBOT ---
            if (isHumanTurn) {
                if (g.setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
                    playHumanSound()
                    boardView?.invalidate()

                    val winner = g.checkForWinner()
                    if (winner == 0) {
                        isHumanTurn = false
                        tvInfo?.text = "Pensando... 🤖"

                        Handler(Looper.getMainLooper()).postDelayed({
                            if (isGameActive) {
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
                            }
                        }, 800)
                    } else {
                        updateGameStatus(winner)
                    }
                }
            }
        } else {
            // --- MODO 2 JUGADORES ---
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

    private fun updateGameStatus(winner: Int) {
        when (winner) {
            1 -> tvInfo?.text = "¡Es un empate! 🤝"
            2 -> tvInfo?.text = if (isVsCpu) "¡Ganaste 🐻!" else "¡Ganó Jugador 1 🐻!"
            3 -> tvInfo?.text = if (isVsCpu) "¡Ganó el Robot 🤖!" else "¡Ganó Jugador 2 🐼!"
        }
        isGameActive = false
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
            // Inicialización segura de audios
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