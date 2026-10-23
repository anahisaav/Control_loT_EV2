package com.example.ev2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import android.widget.TextView

class ConexionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_conexion)

        val txtTituloConexion = findViewById<TextView>(R.id.txtTituloConexion)
        val txtEstadoConexion = findViewById<TextView>(R.id.txtEstadoConexion)
        val txtDispositivo = findViewById<TextView>(R.id.txtDispositivo)

        val btnWiFi = findViewById<Button>(R.id.btnWiFi)
        val btnDesconectar = findViewById<Button>(R.id.btnDesconectar)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        val origen = intent.getStringExtra("ORIGEN")
        txtTituloConexion.text ="Conexion - $origen"

        btnWiFi.setOnClickListener {
            txtEstadoConexion.text = "Estado: Modo Wi-Fi seleccionado"
            txtDispositivo.text = "Dispostivo: ESP32 pendiente"
        }

        btnDesconectar.setOnClickListener {
            txtEstadoConexion.text = "Estado: Sin Conexion"
            txtDispositivo.text = "Dispostivo: --"
        }

        btnVolver.setOnClickListener {
            finish()
        }


    }
}