package co.edu.unal.tictactoe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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

        // 3. Botón 2 JUGADORES
        findViewById<Button>(R.id.btnVsHuman)?.setOnClickListener {
            startNewGame(isVsCpu = false)
        }

        // 4. Botón MULTIJUGADOR ONLINE
        findViewById<Button>(R.id.btnOnline)?.setOnClickListener {
            showOnlineMenuDialog()
        }

        // 5. Botón DIFICULTAD
        findViewById<Button>(R.id.btnDifficulty)?.setOnClickListener {
            showDifficultyDialog()
        }

        // 6. Botón ACERCA DE
        findViewById<Button>(R.id.btnAbout)?.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun startNewGame(isVsCpu: Boolean) {
        prefs?.edit()?.putBoolean("mLastVsCpu", isVsCpu)?.apply()

        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_VS_CPU", isVsCpu)
            putExtra("EXTRA_NEW_GAME", true)
            putExtra("EXTRA_DIFFICULTY", selectedDifficulty)
        }
        startActivity(intent)
    }

    private fun showOnlineMenuDialog() {
        val options = arrayOf("Crear una Sala nueva", "Unirse a una Sala existente")
        AlertDialog.Builder(this)
            .setTitle("Multijugador Online")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> createOnlineRoom()
                    1 -> showJoinRoomDialog()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun createOnlineRoom() {
        val roomId = (100000..999999).random().toString()
        val roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)

        val roomData = mapOf(
            "roomId" to roomId,
            "playerX" to "host",
            "playerO" to "",
            "turn" to "X",
            "gameStatus" to "waiting",
            "board" to listOf("", "", "", "", "", "", "", "", "")
        )

        roomRef.setValue(roomData).addOnSuccessListener {
            AlertDialog.Builder(this)
                .setTitle("¡Sala Creada!")
                .setMessage("Comparte este código de 6 dígitos con tu amigo:\n\n$roomId")
                .setPositiveButton("Entrar a la Sala") { _, _ ->
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("EXTRA_ROOM_ID", roomId)
                        putExtra("EXTRA_MY_ROLE", "X")
                        putExtra("EXTRA_IS_ONLINE", true)
                    }
                    startActivity(intent)
                }
                .setNegativeButton("Cancelar") { _, _ ->
                    roomRef.removeValue()
                }
                .setCancelable(false)
                .show()
        }.addOnFailureListener {
            Toast.makeText(this, "Error al crear la sala en Firebase", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showJoinRoomDialog() {
        val inputCode = EditText(this).apply {
            hint = "Ej: 123456"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(this)
            .setTitle("Unirse a una Sala")
            .setMessage("Ingresa el código de 6 dígitos provisto por tu amigo:")
            .setView(inputCode)
            .setPositiveButton("Unirse") { _, _ ->
                val roomId = inputCode.text.toString().trim()
                if (roomId.length == 6) {
                    joinOnlineRoom(roomId)
                } else {
                    Toast.makeText(this, "Ingresa un código válido de 6 dígitos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun joinOnlineRoom(roomId: String) {
        val roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId)

        roomRef.get().addOnSuccessListener { snapshot: DataSnapshot ->
            if (snapshot.exists()) {
                val status = snapshot.child("gameStatus").value as? String
                if (status == "waiting") {
                    roomRef.child("playerO").setValue("guest")
                    roomRef.child("gameStatus").setValue("playing")

                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("EXTRA_ROOM_ID", roomId)
                        putExtra("EXTRA_MY_ROLE", "O")
                        putExtra("EXTRA_IS_ONLINE", true)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "La sala ya está ocupada o finalizó", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "La sala no existe", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Error de conexión con Firebase", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDifficultyDialog() {
        val levels = arrayOf("Fácil", "Medio", "Difícil (Imposible)")
        AlertDialog.Builder(this)
            .setTitle("Selecciona la Dificultad")
            .setSingleChoiceItems(levels, selectedDifficulty) { dialog, which ->
                selectedDifficulty = which
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