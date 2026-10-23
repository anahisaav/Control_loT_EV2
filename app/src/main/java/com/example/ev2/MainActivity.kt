package com.example.ev2

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Control de acceso: sin sesion no se muestra el panel
        if (Sesion.usuario == null) {
            irAlLogin()
            return
        }

        setContentView(R.layout.activity_main)

        val txtUsuario = findViewById<TextView>(R.id.txtUsuario)
        val btnSensor = findViewById<Button>(R.id.btnSensor)
        val btnConexion = findViewById<Button>(R.id.btnConfigurar)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        txtUsuario.text = "Usuario: ${Sesion.usuario}"

        btnSensor.setOnClickListener {
            startActivity(Intent(this, SensorActivity::class.java))
        }

        btnConexion.setOnClickListener {
            val intent = Intent(this, ConexionActivity::class.java)
            intent.putExtra("ORIGEN", "Control IoT")
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            Sesion.usuario = null
            irAlLogin()
        }
    }

    private fun irAlLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
