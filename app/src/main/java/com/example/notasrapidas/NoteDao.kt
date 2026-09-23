package com.example.notasrapidas

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object): define las operaciones que se pueden hacer
 * sobre la tabla "notes". Room genera automáticamente el código SQL
 * necesario a partir de estas anotaciones.
 */
@Dao
interface NoteDao {

    // Devuelve todas las notas ordenadas de la más reciente a la más antigua.
    // Flow permite que la UI se actualice automáticamente cuando los datos cambian.
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    // Inserta una nota nueva. Si hay conflicto de id, la reemplaza.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    // Actualiza una nota existente (por ejemplo, al editar título o contenido).
    @Update
    suspend fun updateNote(note: Note)

    // Elimina una nota específica.
    @Delete
    suspend fun deleteNote(note: Note)
}
