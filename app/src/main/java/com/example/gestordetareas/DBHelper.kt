package com.example.gestordetareas

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class DBHelper(context: Context) : SQLiteOpenHelper(context, "TareasDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTasksTable = """
        CREATE TABLE tasks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT,
            description TEXT,
            estado TEXT,
            fecha TEXT
        )
    """.trimIndent()

        val createUsersTable = """
        CREATE TABLE users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE,
            password TEXT
        )
    """.trimIndent()

        db.execSQL(createTasksTable)
        db.execSQL(createUsersTable)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        onCreate(db)
    }

    fun insertTask(name: String, description: String, estado: String, fecha: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("estado", estado)
            put("fecha", fecha)
        }
        val result = db.insert("tasks", null, values)
        return result != -1L
    }

    fun getAllTasks(): List<Pair<Int, String>> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, name, description, estado, fecha FROM tasks", null)

        val tasks = mutableListOf<Pair<Int, String>>()
        while (cursor.moveToNext()) {
            val id = cursor.getInt(0)
            val name = cursor.getString(1)
            val description = cursor.getString(2)
            val estado = cursor.getString(3)
            val fecha = cursor.getString(4)
            val taskText = "\n 📝 $name\n\n📄 $description\n\n📅 $fecha   ✅ $estado"
            tasks.add(Pair(id, taskText))
        }
        cursor.close()
        return tasks
    }

    fun getTaskById(id: Int): Task? {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT id, name, description, estado, fecha FROM tasks WHERE id = ?",
            arrayOf(id.toString())
        )
        return if (cursor.moveToFirst()) {
            val task = Task(
                id = cursor.getInt(0),
                name = cursor.getString(1),
                description = cursor.getString(2),
                estado = cursor.getString(3),
                fecha = cursor.getString(4)
            )
            cursor.close()
            task
        } else {
            cursor.close()
            null
        }
    }

    fun updateTask(id: Int, name: String, description: String, estado: String, fecha: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", name)
            put("description", description)
            put("estado", estado)
            put("fecha", fecha)
        }
        val result = db.update("tasks", values, "id = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun deleteTaskById(id: Int): Boolean {
        val db = writableDatabase
        val result = db.delete("tasks", "id = ?", arrayOf(id.toString()))
        return result > 0
    }

    fun insertUser(username: String, encryptedPassword: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("username", username)
            put("password", encryptedPassword)
        }
        return db.insert("users", null, values) != -1L
    }

    fun authenticateUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val query = "SELECT * FROM users WHERE username = ? AND password = ?"
        val cursor = db.rawQuery(query, arrayOf(username, password))
        val success = cursor.moveToFirst()
        cursor.close()
        return success
    }

    fun deleteUserByUsername(username: String): Boolean {
        val db = writableDatabase
        return db.delete("users", "username = ?", arrayOf(username)) > 0
    }

    fun deleteAllTasks(): Boolean {
        val db = writableDatabase
        return db.delete("tasks", null, null) > 0
    }



    fun getEncryptedPassword(username: String): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT password FROM users WHERE username = ?", arrayOf(username))
        var encrypted: String? = null
        if (cursor.moveToFirst()) {
            encrypted = cursor.getString(0)
        }
        cursor.close()
        return encrypted
    }

}


data class Task(
    val id: Int,
    val name: String,
    val description: String,
    val estado: String,
    val fecha: String
)