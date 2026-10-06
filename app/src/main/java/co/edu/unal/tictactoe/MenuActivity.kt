package co.edu.unal.tictactoe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    private var selectedDifficulty = 0 // 0: Fácil, 1: Medio, 2: Difícil
    private var prefs: SharedPreferences? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        prefs = getSharedPreferences("ttt_prefs", Context.MODE_PRIVATE)
        selectedDifficulty = prefs?.getInt("mDifficulty", 0) ?: 0

        // 1. Botón CONTINUAR (Reanuda el último modo jugado)
        findViewById<Button>(R.id.btnContinue)?.setOnClickListener {
            val lastVsCpu = prefs?.getBoolean("mLastVsCpu", true) ?: true
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_VS_CPU", lastVsCpu)
                putExtra("EXTRA_NEW_GAME", false)
            }
            startActivity(intent)
        }

        // 2. Botón JUGAR VS CPU
        findViewById<Button>(R.id.btnVsCpu)?.setOnClickListener {
            startNewGame(isVsCpu = true)
        }

        // 3. Botón 2 JUGADORES (Coincide con @+id/btnVsHuman en el XML)
        findViewById<Button>(R.id.btnVsHuman)?.setOnClickListener {
            startNewGame(isVsCpu = false)
        }

        // 4. Botón DIFICULTAD
        findViewById<Button>(R.id.btnDifficulty)?.setOnClickListener {
            showDifficultyDialog()
        }

        // 5. Botón ACERCA DE
        findViewById<Button>(R.id.btnAbout)?.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun startNewGame(isVsCpu: Boolean) {
        // Guardar modalidad para que "Continuar" la recuerde
        prefs?.edit()?.putBoolean("mLastVsCpu", isVsCpu)?.apply()

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_VS_CPU", isVsCpu)
            putExtra("EXTRA_NEW_GAME", true)
            putExtra("EXTRA_DIFFICULTY", selectedDifficulty)
        }
        startActivity(intent)
    }

    private fun showDifficultyDialog() {
        val levels = arrayOf("Fácil", "Medio", "Difícil (Imposible)")
        AlertDialog.Builder(this)
            .setTitle("Selecciona la Dificultad")
            .setSingleChoiceItems(levels, selectedDifficulty) { dialog, which ->
                selectedDifficulty = which

                // Guardar la dificultad en SharedPreferences
                prefs?.edit()?.putInt("mDifficulty", selectedDifficulty)?.apply()

                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Acerca de Ositos Tic Tac Toe")
            .setMessage("Desarrollado para la materia Desarrollo de Aplicaciones Móviles.\n\nUniversidad Nacional de Colombia.")
            .setPositiveButton("Aceptar", null)
            .show()
    }
}