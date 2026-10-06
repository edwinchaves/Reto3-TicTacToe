package co.edu.unal.tictactoe

class TicTacToeGame {

    companion object {
        const val BOARD_SIZE = 9
        const val OPEN_SPOT = ' '
        const val HUMAN_PLAYER = 'X'
        const val COMPUTER_PLAYER = 'O'
    }

    // Enum para los niveles de dificultad
    enum class DifficultyLevel { Easy, Harder, Expert }

    // Cambiado a private para evitar el choque de getters/setters automáticos en JVM
    private var difficultyLevel: DifficultyLevel = DifficultyLevel.Expert
    private var mBoard = CharArray(BOARD_SIZE) { OPEN_SPOT }

    fun clearBoard() {
        for (i in 0 until BOARD_SIZE) {
            mBoard[i] = OPEN_SPOT
        }
    }

    fun getBoardOccupant(location: Int): Char {
        if (location in 0 until BOARD_SIZE) {
            return mBoard[location]
        }
        return OPEN_SPOT
    }

    // --- MÉTODOS PARA GUARDAR Y RESTAURAR ESTADO ---

    /**
     * Retorna una copia del estado actual del tablero para guardarlo en onSaveInstanceState
     */
    fun getBoardState(): CharArray {
        return mBoard.clone()
    }

    /**
     * Restaura el tablero desde una copia guardada en onRestoreInstanceState
     */
    fun setBoardState(board: CharArray) {
        mBoard = board.clone()
    }

    // --- MÉTODOS PARA MANEJAR DIFICULTAD ---

    fun getDifficultyLevel(): DifficultyLevel = difficultyLevel

    /**
     * Establece la dificultad según el entero guardado en SharedPreferences (0: Easy, 1: Harder, 2: Expert)
     */
    fun setDifficultyLevel(difficulty: Int) {
        difficultyLevel = when (difficulty) {
            0 -> DifficultyLevel.Easy
            1 -> DifficultyLevel.Harder
            else -> DifficultyLevel.Expert
        }
    }

    /**
     * Sobrecarga por si se pasa directamente un enum
     */
    fun setDifficultyLevel(level: DifficultyLevel) {
        this.difficultyLevel = level
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

        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) return 0
        }

        return 1
    }

    // --- ALGORITMOS DE DIFICULTAD ---

    fun getRandomMove(): Int {
        val availableMoves = mutableListOf<Int>()
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                availableMoves.add(i)
            }
        }
        return if (availableMoves.isNotEmpty()) availableMoves.random() else -1
    }

    fun getWinningMove(): Int {
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = COMPUTER_PLAYER
                val isWin = checkForWinner() == 3
                mBoard[i] = OPEN_SPOT
                if (isWin) return i
            }
        }
        return -1
    }

    fun getBlockingMove(): Int {
        for (i in 0 until BOARD_SIZE) {
            if (mBoard[i] == OPEN_SPOT) {
                mBoard[i] = HUMAN_PLAYER
                val isHumanWin = checkForWinner() == 2
                mBoard[i] = OPEN_SPOT
                if (isHumanWin) return i
            }
        }
        return -1
    }

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