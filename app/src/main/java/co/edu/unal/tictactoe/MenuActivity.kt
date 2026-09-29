package co.edu.unal.tictactoe

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val btnVsCpu = findViewById<Button>(R.id.btnVsCpu)
        val btnVsHuman = findViewById<Button>(R.id.btnVsHuman)

        btnVsCpu?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_VS_CPU", true)
            }
            startActivity(intent)
        }

        btnVsHuman?.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("EXTRA_VS_CPU", false)
            }
            startActivity(intent)
        }
    }
}