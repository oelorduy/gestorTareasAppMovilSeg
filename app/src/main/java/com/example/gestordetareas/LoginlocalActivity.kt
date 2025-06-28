package com.example.gestordetareas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

class LoginlocalActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var dbHelper: DBHelper
    private lateinit var aesKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginlocal)

        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Configurar EncryptedSharedPreferences
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Mostrar el correo guardado (si existe)
        val savedUsername = sharedPreferences.getString("username", null)
        if (!savedUsername.isNullOrEmpty()) {
            emailEditText.setText(savedUsername)
        }

        // Generar clave AES si no existe
        if (!sharedPreferences.contains("aes_key")) {
            val keyGen = KeyGenerator.getInstance("AES")
            keyGen.init(128)
            val secretKey = keyGen.generateKey()
            val keyBase64 = Base64.encodeToString(secretKey.encoded, Base64.NO_WRAP)
            sharedPreferences.edit().putString("aes_key", keyBase64).apply()
        }

        aesKey = sharedPreferences.getString("aes_key", "")!!
        dbHelper = DBHelper(this)

        loginButton.setOnClickListener {
            val username = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
                Toast.makeText(this, "El correo no tiene un formato válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val encryptedInputPassword = encryptPassword(password, aesKey)

            val storedEncryptedPassword = dbHelper.getEncryptedPassword(username)
            if (storedEncryptedPassword != null && storedEncryptedPassword == encryptedInputPassword) {
                sharedPreferences.edit().putString("username", username).apply()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else if (storedEncryptedPassword == null) {
                // Registro automático
                val success = dbHelper.insertUser(username, encryptedInputPassword)
                if (success) {
                    sharedPreferences.edit().putString("username", username).apply()
                    Toast.makeText(this, "Usuario registrado correctamente", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Error al registrar usuario", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun encryptPassword(password: String, keyBase64: String): String {
        val keyBytes = Base64.decode(keyBase64, Base64.NO_WRAP)
        val secretKeySpec = SecretKeySpec(keyBytes, "AES")
        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
        val encrypted = cipher.doFinal(password.toByteArray())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
}