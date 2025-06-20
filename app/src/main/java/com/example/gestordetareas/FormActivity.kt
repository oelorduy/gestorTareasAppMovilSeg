package com.example.gestordetareas

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class FormActivity : AppCompatActivity() {

    private var taskId: Int? = null
    private lateinit var dbHelper: DBHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        val etTaskName = findViewById<EditText>(R.id.etTaskName)
        val etTaskDescription = findViewById<EditText>(R.id.etTaskDescription)
        val btnSaveTask = findViewById<Button>(R.id.btnSaveTask)
        val btnBack = findViewById<Button>(R.id.btnBack)
        val spinnerEstado = findViewById<Spinner>(R.id.spinnerEstado)
        val etFecha = findViewById<EditText>(R.id.etFecha)

        dbHelper = DBHelper(this)

        // Configurar estados
        val estados = listOf("Pendiente", "Completado")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, estados)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerEstado.adapter = adapter

        // Obtener ID de tarea (si viene para editar)
        taskId = intent.getIntExtra("taskId", -1).takeIf { it != -1 }

        // Si es edición, llenar campos
        taskId?.let { id ->
            val task = dbHelper.getTaskById(id)
            task?.let {
                etTaskName.setText(it.name)
                etTaskDescription.setText(it.description)
                etFecha.setText(it.fecha)
                val index = estados.indexOf(it.estado)
                if (index >= 0) spinnerEstado.setSelection(index)
            }
        }

        // Calendario para seleccionar fecha
        etFecha.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, y, m, d ->
                val fechaSeleccionada = String.format("%02d/%02d/%04d", d, m + 1, y)
                etFecha.setText(fechaSeleccionada)
            }, year, month, day)

            datePickerDialog.show()
        }

        btnSaveTask.setOnClickListener {
            val name = etTaskName.text.toString().trim()
            val description = etTaskDescription.text.toString().trim()
            val estado = spinnerEstado.selectedItem.toString()
            val fecha = etFecha.text.toString().trim()

            if (name.isEmpty() || description.isEmpty() || fecha.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val success = if (taskId == null) {
                dbHelper.insertTask(name, description, estado, fecha)
            } else {
                dbHelper.updateTask(taskId!!, name, description, estado, fecha)
            }

            if (success) {
                Toast.makeText(this, "Tarea guardada correctamente", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Error al guardar la tarea", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}