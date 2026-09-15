package co.edu.unal.tictactoe

class TicTacToeGame {

    companion object {
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
        const val OPEN_SPOT = ' '
        const val BOARD_SIZE = 9
    }

    private val board = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun clearBoard() {
        for (i in 0 until BOARD_SIZE) {
            board[i] = OPEN_SPOT
        }
    }

    fun setMove(player: Char, location: Int): Boolean {
        if (location in 0 until BOARD_SIZE && board[location] == OPEN_SPOT) {
            board[location] = player
            return true
        }
        return false
    }

    /**
     * Calcula el mejor movimiento para el Robot usando el algoritmo Minimax.
     */
    fun getComputerMove(): Int {
        var bestScore = Int.MIN_VALUE
        var bestMove = -1

        for (i in 0 until BOARD_SIZE) {
            if (board[i] == OPEN_SPOT) {
                board[i] = COMPUTER_PLAYER
                val score = minimax(0, false)
                board[i] = OPEN_SPOT // Deshacer movimiento

                if (score > bestScore) {
                    bestScore = score
                    bestMove = i
                }
            }
        }
        return bestMove
    }

    private fun minimax(depth: Int, isMaximizing: Boolean): Int {
        val winner = checkForWinner()
        if (winner == 3) return 10 - depth  // Gana la CPU
        if (winner == 2) return depth - 10  // Gana el Humano
        if (winner == 1) return 0           // Empate

        if (isMaximizing) {
            var bestScore = Int.MIN_VALUE
            for (i in 0 until BOARD_SIZE) {
                if (board[i] == OPEN_SPOT) {
                    board[i] = COMPUTER_PLAYER
                    val score = minimax(depth + 1, false)
                    board[i] = OPEN_SPOT
                    bestScore = maxOf(score, bestScore)
                }
            }
            return bestScore
        } else {
            var bestScore = Int.MAX_VALUE
            for (i in 0 until BOARD_SIZE) {
                if (board[i] == OPEN_SPOT) {
                    board[i] = HUMAN_PLAYER
                    val score = minimax(depth + 1, true)
                    board[i] = OPEN_SPOT
                    bestScore = minOf(score, bestScore)
                }
            }
            return bestScore
        }
    }

    /**
     * Retorna:
     * 0 -> El juego continúa
     * 1 -> Empate
     * 2 -> Gana el Humano / Jugador 1
     * 3 -> Gana la CPU / Jugador 2
     */
    fun checkForWinner(): Int {
        val winningCombos = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8),
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8),
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)
        )

        for (combo in winningCombos) {
            if (board[combo[0]] != OPEN_SPOT &&
                board[combo[0]] == board[combo[1]] &&
                board[combo[1]] == board[combo[2]]
            ) {
                return if (board[combo[0]] == HUMAN_PLAYER) 2 else 3
            }
        }

        for (i in 0 until BOARD_SIZE) {
            if (board[i] == OPEN_SPOT) {
                return 0
            }
        }

        return 1
    }
}