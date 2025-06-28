package com.example.gestordetareas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {

    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar preferencias cifradas
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        // Verificar que exista sesión
        val savedUsername = sharedPreferences.getString("username", null)
        if (savedUsername.isNullOrEmpty()) {
            startActivity(Intent(this, LoginlocalActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_settings)

        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val dellogoutButton = findViewById<MaterialButton>(R.id.dellogoutButton)
        val mapButton = findViewById<Button>(R.id.btnOpenMap)
        val btnBack = findViewById<Button>(R.id.btnBack)

        dbHelper = DBHelper(this)

        //  Cerrar sesión sin borrar cuenta
        logoutButton.setOnClickListener {
            sharedPreferences.edit().remove("username").apply()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginlocalActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // 🗑 Eliminar usuario, tareas y cerrar sesión
        dellogoutButton.setOnClickListener {
            val username = sharedPreferences.getString("username", null)

            if (!username.isNullOrEmpty()) {
                val userDeleted = dbHelper.deleteUserByUsername(username)
                val tasksDeleted = dbHelper.deleteAllTasks()

                sharedPreferences.edit().clear().apply()

                val msg = when {
                    userDeleted && tasksDeleted -> "Usuario y tareas eliminados correctamente"
                    userDeleted -> "Usuario eliminado, sin tareas registradas"
                    else -> "Error al eliminar el usuario"
                }

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show()
            }

            val intent = Intent(this, LoginlocalActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        mapButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}