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
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var taskAdapter: ArrayAdapter<String>
    private val tasksList = mutableListOf<String>()
    private val taskIdList = mutableListOf<Int>()
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

        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listTasks)
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val btnSetting = findViewById<FloatingActionButton>(R.id.btnSettings)


        dbHelper = DBHelper(this)

        btnSetting.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java)) // reemplaza por tu pantalla de configuración
        }



        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasksList)
        listView.adapter = taskAdapter

        btnAddTask.setOnClickListener {
            startActivity(Intent(this, FormActivity::class.java))
        }

        // Eliminar tarea al dejar presionado
        listView.setOnItemLongClickListener { _, _, position, _ ->
            val taskText = tasksList[position]
            val taskId = taskIdList[position]

            AlertDialog.Builder(this)
                .setTitle("Eliminar tarea")
                .setMessage("¿Deseas eliminar esta tarea?\n\n$taskText")
                .setPositiveButton("Sí") { _, _ ->
                    if (dbHelper.deleteTaskById(taskId)) {
                        Toast.makeText(this, "Tarea eliminada", Toast.LENGTH_SHORT).show()
                        loadTasksFromDB()
                    } else {
                        Toast.makeText(this, "Error al eliminar tarea", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("No", null)
                .show()
            true
        }

        // Editar tarea al hacer clic
        listView.setOnItemClickListener { _, _, position, _ ->
            val taskId = taskIdList[position]
            val intent = Intent(this, FormActivity::class.java)
            intent.putExtra("taskId", taskId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadTasksFromDB()
    }

    private fun loadTasksFromDB() {
        tasksList.clear()
        taskIdList.clear()

        val tasks = dbHelper.getAllTasks()
        for ((id, text) in tasks) {
            taskIdList.add(id)
            tasksList.add(text)
        }

        taskAdapter.notifyDataSetChanged()
    }
}