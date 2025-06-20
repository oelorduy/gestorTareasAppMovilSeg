package com.example.gestordetareas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SettingsActivity : AppCompatActivity() {


    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔐 Verificación de sesión antes de cargar la interfaz
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val sharedPreferences = EncryptedSharedPreferences.create(
            "secure_prefs",
            masterKeyAlias,
            applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val savedEmail = sharedPreferences.getString("email", null)
        val savedPassword = sharedPreferences.getString("password", null)

        if (savedEmail.isNullOrEmpty() || savedPassword.isNullOrEmpty()) {
            // Si no hay sesión válida, redirigir al login
            startActivity(Intent(this, LoginlocalActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_settings)

        val dellogoutButton = findViewById<MaterialButton>(R.id.dellogoutButton)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val mapButton = findViewById<Button>(R.id.btnOpenMap)
        val btnBack = findViewById<Button>(R.id.btnBack)

        dbHelper = DBHelper(this)


       logoutButton.setOnClickListener {
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginlocalActivity::class.java))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            finish()
        }

        mapButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }

        dellogoutButton.setOnClickListener {
                sharedPreferences.edit().clear().apply()
                Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(Intent(this, LoginlocalActivity::class.java))
                finish()

        }





    }


}