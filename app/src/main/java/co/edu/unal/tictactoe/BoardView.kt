package co.edu.unal.tictactoe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

class BoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val GRID_WIDTH = 10f
    }

    private var humanBitmap: Bitmap? = null
    private var computerBitmap: Bitmap? = null

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7CA1D8")
        strokeWidth = GRID_WIDTH
        style = Paint.Style.STROKE
    }

    private val humanFallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F28B82")
        strokeWidth = 12f
        style = Paint.Style.STROKE
    }

    private val cpuFallbackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#7CA1D8")
        strokeWidth = 12f
        style = Paint.Style.STROKE
    }

    private var game: TicTacToeGame? = null

    init {
        // Blindaje contra errores de decodificación o lectura de recursos
        try {
            humanBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_bear_human)
            computerBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_bear_robot)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    fun setGame(game: TicTacToeGame) {
        this.game = game
    }

    fun setPlayerBitmaps(humanResId: Int, computerResId: Int) {
        try {
            humanBitmap = BitmapFactory.decodeResource(resources, humanResId)
            computerBitmap = BitmapFactory.decodeResource(resources, computerResId)
            invalidate()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val boardWidth = width
        val boardHeight = height
        if (boardWidth <= 0 || boardHeight <= 0) return

        val cellWidth = boardWidth / 3
        val cellHeight = boardHeight / 3

        // Dibujar las líneas de la cuadrícula
        canvas.drawLine(cellWidth.toFloat(), 0f, cellWidth.toFloat(), boardHeight.toFloat(), gridPaint)
        canvas.drawLine((cellWidth * 2).toFloat(), 0f, (cellWidth * 2).toFloat(), boardHeight.toFloat(), gridPaint)

        canvas.drawLine(0f, cellHeight.toFloat(), boardWidth.toFloat(), cellHeight.toFloat(), gridPaint)
        canvas.drawLine(0f, (cellHeight * 2).toFloat(), boardWidth.toFloat(), (cellHeight * 2).toFloat(), gridPaint)

        // Dibujar fichas del juego
        val currentGame = game ?: return
        for (i in 0 until TicTacToeGame.BOARD_SIZE) {
            val col = i % 3
            val row = i / 3
            val rect = Rect(
                col * cellWidth,
                row * cellHeight,
                (col + 1) * cellWidth,
                (row + 1) * cellHeight
            )

            val occupant = currentGame.getBoardOccupant(i)
            if (occupant == TicTacToeGame.HUMAN_PLAYER) {
                if (humanBitmap != null) {
                    canvas.drawBitmap(humanBitmap!!, null, rect, null)
                } else {
                    val margin = cellWidth * 0.25f
                    canvas.drawLine(col * cellWidth + margin, row * cellHeight + margin, (col + 1) * cellWidth - margin, (row + 1) * cellHeight - margin, humanFallbackPaint)
                    canvas.drawLine((col + 1) * cellWidth - margin, row * cellHeight + margin, col * cellWidth + margin, (row + 1) * cellHeight - margin, humanFallbackPaint)
                }
            } else if (occupant == TicTacToeGame.COMPUTER_PLAYER) {
                if (computerBitmap != null) {
                    canvas.drawBitmap(computerBitmap!!, null, rect, null)
                } else {
                    val centerX = col * cellWidth + cellWidth / 2f
                    val centerY = row * cellHeight + cellHeight / 2f
                    val radius = cellWidth * 0.3f
                    canvas.drawCircle(centerX, centerY, radius, cpuFallbackPaint)
                }
            }
        }
    }

    fun getBoardCellWidth(): Int = if (width > 0) width / 3 else 1
    fun getBoardCellHeight(): Int = if (height > 0) height / 3 else 1
}