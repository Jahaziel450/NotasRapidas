package com.example.notasrapidas

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Representa una nota individual dentro de la base de datos.
 *
 * @Entity le indica a Room que esta clase debe convertirse en una tabla SQLite.
 * Cada instancia de Note = una fila en la tabla "notes".
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // Identificador único, se genera automáticamente
    val title: String,        // Título corto de la nota
    val content: String,      // Contenido / cuerpo de la nota
    val timestamp: Long = System.currentTimeMillis() // Fecha de creación/edición, útil para ordenar
) {
    /**
     * Helper function to format the timestamp into a readable date string.
     * Use a format like "dd/MM/yyyy HH:mm" for clarity.
     */
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
