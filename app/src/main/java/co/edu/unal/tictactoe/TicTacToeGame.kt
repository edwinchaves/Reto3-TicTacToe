package co.edu.unal.tictactoe

class TicTacToeGame {

    // Constantes del tablero y jugadores
    companion object {
        const val BOARD_SIZE = 9
        const val OPEN_SPOT = ' '
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
    }

    // Enum para los niveles de dificultad (Reto 4)
    enum class DifficultyLevel { Easy, Harder, Expert }

    var difficultyLevel: DifficultyLevel = DifficultyLevel.Expert
    private val mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun clearBoard() {
        for (i in 0 until BOARD_SIZE) {
            mBoard[i] = OPEN_SPOT
        }
    }

    fun setMove(player: Char, location: Int): Boolean {
        if (location in 0 until BOARD_SIZE && mBoard[location] == OPEN_SPOT) {
            mBoard[location] = player
            return true
        }
        return false
    }

    /**
     * Verifica el estado del juego:
     * 0: Juego en curso
     * 1: Empate
     * 2: Gana Jugador Humano ('X')
     * 3: Gana la Computadora ('O')
     */
    fun checkForWinner(): Int {
        // Combinaciones ganadoras en el tablero de 3x3
        val winLines = arrayOf(
            intArrayOf(0, 1, 2), intArrayOf(3, 4, 5), intArrayOf(6, 7, 8), // Filas
            intArrayOf(0, 3, 6), intArrayOf(1, 4, 7), intArrayOf(2, 5, 8), // Columnas
            intArrayOf(0, 4, 8), intArrayOf(2, 4, 6)                       // Diagonales
        )

        for (line in winLines) {
            if (mBoard[line[0]] != OPEN_SPOT &&
                mBoard[line[0]] == mBoard[line[1]] &&
                mBoard[line[1]] == mBoard[line[2]]
            ) {
                return if (mBoard[line[0]] == HUMAN_PLAYER) 2 else 3
            }
        }

        // Si hay casillas libres, el juego continúa
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) return 0
        }

        // Si no hay ganador ni casillas libres, es empate
        return 1
    }

    // --- ALGORITMOS DE DIFICULTAD (RETO 4) ---

    // Movimiento Aleatorio (Easy)
    fun getRandomMove(): Int {
        val availableMoves = mutableListOf<Int>()
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                availableMoves.add(i)
            }
        }
        return if (availableMoves.isNotEmpty()) availableMoves.random() else -1
    }

    // Movimiento para Ganar (Harder)
    fun getWinningMove(): Int {
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER
                val isWin = checkForWinner() == 3
                mBoard[i] = OPEN_SPOT // Revertir prueba
                if (isWin) return i
            }
        }
        return -1
    }

    // Movimiento para Bloquear (Expert)
    fun getBlockingMove(): Int {
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER
                val isHumanWin = checkForWinner() == 2
                mBoard[i] = OPEN_SPOT // Revertir prueba
                if (isHumanWin) return i
            }
        }
        return -1
    }

    // Determina la casilla seleccionada por la IA según el nivel configurado
    fun getComputerMove(): Int {
        return when (difficultyLevel) {
            DifficultyLevel.Easy -> getRandomMove()
            DifficultyLevel.Harder -> {
                val winMove = getWinningMove()
                if (winMove != -1) winMove else getRandomMove()
            }
            DifficultyLevel.Expert -> {
                val winMove = getWinningMove()
                if (winMove != -1) return winMove
                val blockMove = getBlockingMove()
                if (blockMove != -1) return blockMove
                getRandomMove()
            }
        }
    }
}