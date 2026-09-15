package co.edu.unal.tictactoe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import co.edu.unal.tictactoe.R // <-- IMPORTANTE: Soluciona los 'Unresolved reference R/layout/id/drawable'

@Suppress("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var game: TicTacToeGame
    private lateinit var boardButtons: Array<ImageButton>
    private lateinit var tvInfo: TextView

    private var isVsCpu: Boolean = true
    private var isHumanTurn: Boolean = true
    private var isGameActive: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        game = TicTacToeGame()
        tvInfo = findViewById(R.id.tvInfo)

        boardButtons = arrayOf(
            findViewById(R.id.btn0), findViewById(R.id.btn1), findViewById(R.id.btn2),
            findViewById(R.id.btn3), findViewById(R.id.btn4), findViewById(R.id.btn5),
            findViewById(R.id.btn6), findViewById(R.id.btn7), findViewById(R.id.btn8)
        )

        for (i in boardButtons.indices) {
            boardButtons[i].setOnClickListener {
                if (isGameActive) {
                    onCellClicked(i)
                }
            }
        }

        val btnVsCpu = findViewById<Button>(R.id.btnModeVsCpu)
        val btnVsHuman = findViewById<Button>(R.id.btnModeVsHuman)
        val btnReset = findViewById<Button>(R.id.btnReset)

        btnVsCpu.setOnClickListener {
            isVsCpu = true
            startNewGame()
        }

        btnVsHuman.setOnClickListener {
            isVsCpu = false
            startNewGame()
        }

        btnReset.setOnClickListener {
            startNewGame()
        }

        startNewGame()
    }

    private fun startNewGame() {
        game.clearBoard()
        isGameActive = true
        isHumanTurn = true

        for (i in boardButtons.indices) {
            boardButtons[i].setImageResource(0)
            boardButtons[i].isEnabled = true
        }

        if (isVsCpu) {
            tvInfo.text = "Modo: Vs Robot 🤖 (Tu turno)"
        } else {
            tvInfo.text = "Modo 2 Jugadores: Turno Jugador 1 🐻"
        }
    }

    private fun onCellClicked(location: Int) {
        if (!isGameActive) return

        if (isVsCpu) {
            if (game.setMove(TicTacToeGame.HUMAN_PLAYER, location)) {
                boardButtons[location].setImageResource(R.drawable.ic_bear_human)
                boardButtons[location].isEnabled = false

                val winner = game.checkForWinner()
                if (winner == 0) {
                    tvInfo.text = "Pensando... 🤖"
                    setBoardEnabled(false)

                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isGameActive) {
                            val move = game.getComputerMove()
                            if (move != -1) {
                                game.setMove(TicTacToeGame.COMPUTER_PLAYER, move)
                                boardButtons[move].setImageResource(R.drawable.ic_bear_robot)
                                boardButtons[move].isEnabled = false

                                val winnerAfterCpu = game.checkForWinner()
                                if (winnerAfterCpu == 0) {
                                    tvInfo.text = "¡Es tu turno!"
                                    setBoardEnabled(true)
                                } else {
                                    updateGameStatus(winnerAfterCpu)
                                }
                            }
                        }
                    }, 1000)

                } else {
                    updateGameStatus(winner)
                }
            }
        } else {
            val currentPlayer = if (isHumanTurn) TicTacToeGame.HUMAN_PLAYER else TicTacToeGame.COMPUTER_PLAYER
            val currentDrawable = if (isHumanTurn) R.drawable.ic_bear_human else R.drawable.ic_bear_human2

            if (game.setMove(currentPlayer, location)) {
                boardButtons[location].setImageResource(currentDrawable)
                boardButtons[location].isEnabled = false

                val winner = game.checkForWinner()
                if (winner == 0) {
                    isHumanTurn = !isHumanTurn
                    tvInfo.text = if (isHumanTurn) "Turno Jugador 1 🐻" else "Turno Jugador 2 🐼"
                } else {
                    updateGameStatus(winner)
                }
            }
        }
    }

    private fun updateGameStatus(winner: Int) {
        when (winner) {
            1 -> tvInfo.text = "¡Es un empate! 🤝"
            2 -> tvInfo.text = if (isVsCpu) "¡Ganaste!" else "¡Gana Jugador 1 🐻!"
            3 -> tvInfo.text = if (isVsCpu) "¡Gano el Robot 🤖!" else "¡Gana Jugador 2 🐼!"
        }

        isGameActive = false
        setBoardEnabled(false)
    }

    private fun setBoardEnabled(enabled: Boolean) {
        for (i in boardButtons.indices) {
            if (enabled) {
                if (game.checkForWinner() == 0 && boardButtons[i].drawable == null) {
                    boardButtons[i].isEnabled = true
                }
            } else {
                boardButtons[i].isEnabled = false
            }
        }
    }
}