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
 * sobre la tabla "notes".
 */
@Dao
interface NoteDao {

    // Devuelve las notas ordenadas: primero las ancladas (isPinned DESC), luego por posición y fecha
    @Query("SELECT * FROM notes ORDER BY isPinned DESC, orderPosition ASC, timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Update
    suspend fun updateNotes(notes: List<Note>)

    @Delete
    suspend fun deleteNote(note: Note)
}
