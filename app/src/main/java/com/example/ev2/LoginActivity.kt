package com.example.ev2

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var db: UsuarioDbHelper
    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button
    private lateinit var txtMensaje: TextView

    private var intentosFallidos = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = UsuarioDbHelper(this)

        edtUsuario = findViewById(R.id.edtUsuario)
        edtPassword = findViewById(R.id.edtPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        txtMensaje = findViewById(R.id.txtMensajeLogin)

        btnIngresar.setOnClickListener { ingresar() }
        btnRegistrar.setOnClickListener { registrar() }
    }

    private fun ingresar() {
        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        if (usuario.isEmpty() || password.isEmpty()) {
            txtMensaje.text = "Complete usuario y contraseña"
            return
        }

        if (db.validar(usuario, password)) {
            intentosFallidos = 0
            Sesion.usuario = usuario
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            intentosFallidos++
            edtPassword.text.clear()
            txtMensaje.text = "Credenciales incorrectas"
            if (intentosFallidos >= 3) bloquearTemporalmente()
        }
    }

    private fun registrar() {
        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        if (usuario.length < 3 || password.length < 6) {
            txtMensaje.text = "Usuario mínimo 3 caracteres y contraseña mínimo 6"
            return
        }

        if (db.registrar(usuario, password)) {
            txtMensaje.text = "Usuario registrado. Ya puede ingresar"
        } else {
            txtMensaje.text = "Ese usuario ya existe"
        }
        edtPassword.text.clear()
    }

    // Control de acceso: tras 3 fallos se bloquea el boton 30 segundos
    private fun bloquearTemporalmente() {
        btnIngresar.isEnabled = false
        object : CountDownTimer(30000, 1000) {
            override fun onTick(msRestantes: Long) {
                txtMensaje.text = "Demasiados intentos. Espere ${msRestantes / 1000} s"
            }

            override fun onFinish() {
                intentosFallidos = 0
                btnIngresar.isEnabled = true
                txtMensaje.text = ""
            }
        }.start()
    }
}
