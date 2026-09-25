package com.example.notasrapidas

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Representa una nota individual dentro de la base de datos.
 * Incluye campos para anclaje (isPinned) y orden personalizado (orderPosition).
 */
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,          // Identificador único, se genera automáticamente
    val title: String,        // Título corto de la nota
    val content: String,      // Contenido / cuerpo de la nota
    val imageUri: String? = null, // URI o ruta de la imagen adjunta a la nota
    val isPinned: Boolean = false, // True si la nota está anclada al inicio
    val orderPosition: Int = 0,    // Posición de ordenamiento personalizado
    val timestamp: Long = System.currentTimeMillis() // Fecha de creación/edición
) {
    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
