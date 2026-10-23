package com.example.ejercicio

import android.graphics.Color

import android.os.Bundle

import android.os.Handler

import android.os.Looper

import android.widget.Button

import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import com.example.ev2.R
import com.example.ev2.Sesion

import java.net.HttpURLConnection

import java.net.URL

class SensorActivity : AppCompatActivity() {

    private val ipEsp32 = "192.168.4.1"
    private val claveApi = "ev2-sensor-2026"   // debe ser igual a API_KEY del ESP32

    private lateinit var txtDistancia: TextView
    private lateinit var txtAlerta: TextView
    private lateinit var txtEstado: TextView

    @Volatile
    private var consultando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Control de acceso: sin sesion se vuelve al login
        if (Sesion.usuario == null) {
            finish()
            return
        }

        setContentView(R.layout.activity_sensor)

        txtDistancia = findViewById(R.id.txtDistancia)
        txtAlerta = findViewById(R.id.txtAlerta)
        txtEstado = findViewById(R.id.txtEstadoSensor)

        findViewById<Button>(R.id.btnActualizarSensor).setOnClickListener {
            consultarSensor()
        }

        findViewById<Button>(R.id.btnVolverSensor).setOnClickListener {
            finish()
        }

        consultarSensor()   // primera lectura al abrir la pantalla
    }

    private fun consultarSensor() {
        if (consultando) return
        consultando = true
        txtEstado.text = "Estado: Consultando..."

        Thread {
            var conexion: HttpURLConnection? = null
            try {
                val url = URL("http://$ipEsp32/sensor")
                conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "GET"
                conexion.connectTimeout = 3000
                conexion.readTimeout = 3000
                conexion.setRequestProperty("X-API-Key", claveApi)

                when (conexion.responseCode) {
                    200 -> {
                        val respuesta = conexion.inputStream.bufferedReader().use { it.readText() }
                        val datos = respuesta.trim().split(";")
                        if (datos.size >= 2) {
                            mostrarDatos(datos[0], datos[1])
                        } else {
                            mostrarError("Respuesta inválida")
                        }
                    }
                    401 -> mostrarError("Acceso no autorizado")
                    else -> mostrarError("Error HTTP: ${conexion.responseCode}")
                }
            } catch (e: Exception) {
                mostrarError("Sin conexión con ESP32")
            } finally {
                conexion?.disconnect()
                consultando = false
            }
        }.start()
    }

    private fun mostrarDatos(distancia: String, estado: String) {
        runOnUiThread {
            when (estado) {
                "OBJETO_CERCA" -> {
                    txtDistancia.text = "Distancia: $distancia cm"
                    txtAlerta.text = "¡ALERTA! Objeto a 20 cm o menos"
                    txtAlerta.setTextColor(Color.RED)
                    Toast.makeText(this, "¡ALERTA! Objeto cercano", Toast.LENGTH_SHORT).show()
                }
                "SIN_OBSTACULO" -> {
                    txtDistancia.text = "Distancia: $distancia cm"
                    txtAlerta.text = "Sin obstáculo"
                    txtAlerta.setTextColor(Color.rgb(0, 128, 0))
                }
                else -> {
                    txtDistancia.text = "Distancia: fuera de rango"
                    txtAlerta.text = "Sin lectura"
                    txtAlerta.setTextColor(Color.GRAY)
                }
            }
            txtEstado.text = "Estado: Datos recibidos"
        }
    }

    private fun mostrarError(mensaje: String) {
        runOnUiThread {
            txtEstado.text = "Estado: $mensaje"
        }
    }
}
