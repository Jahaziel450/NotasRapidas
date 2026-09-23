package com.example.notasrapidas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

/**
 * Actividad principal (único punto de entrada de la app).
 * Controla la navegación simple entre dos pantallas:
 * 1) Lista de notas
 * 2) Crear / editar nota
 *
 * No usamos una librería de navegación externa a propósito: para una app
 * tan pequeña, un simple "estado de pantalla actual" es más fácil de leer
 * y de explicar en el reporte del proyecto.
 */
class MainActivity : ComponentActivity() {

    // "by viewModels()" crea y conserva el ViewModel mientras viva la Activity,
    // incluso si la pantalla rota o se recompone.
    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Manejamos el estado del tema oscuro a nivel de actividad
            var darkTheme by remember { mutableStateOf(false) }
            
            // Definimos esquemas de colores básicos para soportar el cambio
            val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier,
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        viewModel = viewModel,
                        darkTheme = darkTheme,
                        onToggleTheme = { darkTheme = !darkTheme }
                    )
                }
            }
        }
    }
}

/**
 * Controla qué pantalla se muestra:
 * - null           -> lista de notas
 * - Note(id == 0)  -> pantalla de "nota nueva"
 * - Note existente -> pantalla de edición con los datos ya cargados
 */
@Composable
fun AppNavigation(
    viewModel: NoteViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var pantallaEdicion by remember { mutableStateOf<NotaEditState?>(null) }

    when (val estado = pantallaEdicion) {
        null -> {
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { nota -> pantallaEdicion = NotaEditState(nota) },
                onAddNote = { pantallaEdicion = NotaEditState(null) },
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        }
        else -> {
            NoteEditScreen(
                existingNote = estado.nota,
                viewModel = viewModel,
                onBack = { pantallaEdicion = null },
                darkTheme = darkTheme
            )
        }
    }
}

// Clase auxiliar para representar "estamos en modo edición, con esta nota (o ninguna)".
data class NotaEditState(val nota: Note?)
