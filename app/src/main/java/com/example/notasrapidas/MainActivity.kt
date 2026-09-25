package com.example.notasrapidas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Actividad principal (único punto de entrada de la app).
 * Controla la navegación fluida con animaciones entre la lista de notas y la edición.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: NoteViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val systemInDark = isSystemInDarkTheme()
            var darkTheme by remember { mutableStateOf(systemInDark) }

            // Esquema de colores personalizado para un tema Claro u Oscuro elegante y contrastado
            val colorScheme = if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFFD54F),       // Amarillo cálido para resaltar
                    onPrimary = Color(0xFF1F1B00),
                    surface = Color(0xFF1E1E1E),
                    background = Color(0xFF121212),
                    onBackground = Color(0xFFE0E0E0)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFFD84315),       // Tono ámbar/cálido tipo libreta
                    onPrimary = Color.White,
                    surface = Color(0xFFFFFDE7),
                    background = Color(0xFFFFFDE7),
                    onBackground = Color(0xFF212121)
                )
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
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
 * Controla la navegación con animaciones suaves de deslizamiento e intensidad:
 * - null           -> lista de notas
 * - NoteEditState  -> pantalla de edición/creación de nota
 */
@Composable
fun AppNavigation(
    viewModel: NoteViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    var pantallaEdicion by remember { mutableStateOf<NotaEditState?>(null) }

    AnimatedContent(
        targetState = pantallaEdicion,
        transitionSpec = {
            if (targetState != null) {
                // Al abrir la pantalla de edición: deslicar desde la derecha con fade
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width / 3 } + fadeOut()
                )
            } else {
                // Al volver a la lista: deslizar hacia la derecha con fade
                (slideInHorizontally { width -> -width / 3 } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> width } + fadeOut()
                )
            }
        },
        label = "NavegacionTransicion"
    ) { estado ->
        if (estado == null) {
            NoteListScreen(
                viewModel = viewModel,
                onNoteClick = { nota -> pantallaEdicion = NotaEditState(nota) },
                onAddNote = { pantallaEdicion = NotaEditState(null) },
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme
            )
        } else {
            NoteEditScreen(
                existingNote = estado.nota,
                viewModel = viewModel,
                onBack = { pantallaEdicion = null },
                darkTheme = darkTheme
            )
        }
    }
}

// Clase auxiliar para representar el estado de la pantalla de edición
data class NotaEditState(val nota: Note?)
