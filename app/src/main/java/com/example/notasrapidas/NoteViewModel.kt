package com.example.notasrapidas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel: hace de puente entre la base de datos (Room) y las pantallas (Compose).
 * Así la interfaz nunca habla directamente con la base de datos, lo que hace
 * el código más fácil de mantener y de probar.
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getDatabase(application).noteDao()

    // Estado observable con la lista de notas actual. La UI se suscribe a esto.
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        // Cada vez que la base de datos cambia, actualizamos el estado automáticamente.
        viewModelScope.launch {
            dao.getAllNotes().collectLatest { listaActualizada ->
                _notes.value = listaActualizada
            }
        }
    }

    // Crea una nota nueva o actualiza una existente si ya tiene id > 0.
    fun guardarNota(id: Int, titulo: String, contenido: String) {
        if (titulo.isBlank() && contenido.isBlank()) return // Evita guardar notas vacías

        viewModelScope.launch {
            val nota = Note(
                id = id,
                title = titulo,
                content = contenido
            )
            if (id == 0) {
                dao.insertNote(nota)
            } else {
                dao.updateNote(nota)
            }
        }
    }

    fun eliminarNota(note: Note) {
        viewModelScope.launch {
            dao.deleteNote(note)
        }
    }
}
