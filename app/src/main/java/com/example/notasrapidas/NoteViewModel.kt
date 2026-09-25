package com.example.notasrapidas

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * ViewModel: administra el estado de las notas, búsqueda, anclaje y reordenamiento.
 */
class NoteViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = NoteDatabase.getDatabase(application).noteDao()

    // Texto de búsqueda ingresado por el usuario
    val searchQuery = MutableStateFlow("")

    // Estado observable con la lista de notas filtrada según la búsqueda
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    init {
        // Escuchamos los cambios en la base de datos y en la consulta de búsqueda al mismo tiempo
        viewModelScope.launch {
            combine(dao.getAllNotes(), searchQuery) { listaNotas, query ->
                if (query.isBlank()) {
                    listaNotas
                } else {
                    listaNotas.filter { note ->
                        note.title.contains(query, ignoreCase = true) ||
                                note.content.contains(query, ignoreCase = true)
                    }
                }
            }.collect { listaFiltrada ->
                _notes.value = listaFiltrada
            }
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery.value = newQuery
    }

    // Cambia el estado de anclado de una nota
    fun togglePinNota(note: Note) {
        viewModelScope.launch {
            dao.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    // Intercambia la posición de dos notas al arrastrar
    fun moverNota(fromIndex: Int, toIndex: Int) {
        val listaActual = _notes.value.toMutableList()
        if (fromIndex in listaActual.indices && toIndex in listaActual.indices) {
            val itemMover = listaActual.removeAt(fromIndex)
            listaActual.add(toIndex, itemMover)

            // Asignamos nuevas posiciones de ordenamiento
            val listaActualizada = listaActual.mapIndexed { index, note ->
                note.copy(orderPosition = index)
            }
            _notes.value = listaActualizada

            viewModelScope.launch {
                dao.updateNotes(listaActualizada)
            }
        }
    }

    // Crea una nota nueva o actualiza una existente
    fun guardarNota(
        id: Int,
        titulo: String,
        contenido: String,
        imageUri: String? = null,
        isPinned: Boolean = false,
        orderPosition: Int = 0
    ) {
        if (titulo.isBlank() && contenido.isBlank() && imageUri.isNullOrBlank()) return

        viewModelScope.launch {
            val nota = Note(
                id = id,
                title = titulo,
                content = contenido,
                imageUri = imageUri,
                isPinned = isPinned,
                orderPosition = orderPosition
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
