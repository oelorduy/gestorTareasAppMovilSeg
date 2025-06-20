package com.example.gestordetareas

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity2 : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var taskAdapter: ArrayAdapter<String>
    private val tasksList = mutableListOf<String>()
    private val taskIdList = mutableListOf<Int>()
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listTasks)
        val btnAddTask = findViewById<Button>(R.id.btnAddTask)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val mapButton = findViewById<Button>(R.id.btnOpenMap)

        dbHelper = DBHelper(this)

        logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        mapButton.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        taskAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasksList)
        listView.adapter = taskAdapter

        btnAddTask.setOnClickListener {
            startActivity(Intent(this, FormActivity::class.java))
        }

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