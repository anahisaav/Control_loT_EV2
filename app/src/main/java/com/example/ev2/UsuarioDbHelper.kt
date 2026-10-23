package com.example.ev2

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Base de datos SQLite local. NUNCA guarda la contrasena: solo sal + hash PBKDF2.
class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, "ev2.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE usuarios (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "usuario TEXT UNIQUE NOT NULL, " +
                    "salt TEXT NOT NULL, " +
                    "hash TEXT NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        onCreate(db)
    }

    // PBKDF2 con HMAC-SHA256 (requiere Android 8 / API 26 o superior)
    private fun hashear(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            .generateSecret(spec).encoded
    }

    fun registrar(usuario: String, password: String): Boolean {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val valores = ContentValues().apply {
            put("usuario", usuario)
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("hash", Base64.encodeToString(hashear(password, salt), Base64.NO_WRAP))
        }
        // insert devuelve -1 si el usuario ya existe (UNIQUE)
        return writableDatabase.insert("usuarios", null, valores) != -1L
    }

    fun validar(usuario: String, password: String): Boolean {
        // Consulta parametrizada (?) para evitar inyeccion SQL
        val cursor = readableDatabase.rawQuery(
            "SELECT salt, hash FROM usuarios WHERE usuario = ?",
            arrayOf(usuario)
        )
        return cursor.use { c ->
            if (!c.moveToFirst()) {
                false
            } else {
                val salt = Base64.decode(c.getString(0), Base64.NO_WRAP)
                val esperado = Base64.decode(c.getString(1), Base64.NO_WRAP)
                // Comparacion en tiempo constante
                MessageDigest.isEqual(esperado, hashear(password, salt))
            }
        }
    }
}
