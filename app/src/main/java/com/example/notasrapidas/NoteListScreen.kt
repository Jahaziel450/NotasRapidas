package com.example.notasrapidas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Pantalla principal: muestra todas las notas guardadas en tarjetas.
 * - Toca una nota -> abre la pantalla de edición.
 * - Botón "+" flotante -> crea una nota nueva.
 * - Ícono de basura en cada tarjeta -> elimina esa nota.
 * - Botón de tema -> alterna entre modo claro y oscuro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onNoteClick: (Note) -> Unit,
    onAddNote: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    // collectAsState conecta el StateFlow del ViewModel con la UI de Compose.
    val notas by viewModel.notes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notas Rápidas") },
                actions = {
                    // Toggle para alternar el modo oscuro
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (darkTheme) "Oscuro" else "Claro",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Switch(
                            checked = darkTheme,
                            onCheckedChange = { onToggleTheme() }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Se usa Edit (lápiz) para un diseño que sugiere escritura (pluma)
            // Se aumenta el tamaño del botón usando modifier para que sea más vistoso
            FloatingActionButton(
                onClick = onAddNote,
                modifier = Modifier
                    .size(72.dp) // Tamaño aumentado (el estándar es 56.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Agregar nota",
                    modifier = Modifier.size(32.dp) // Icono también un poco más grande
                )
            }
        }
    ) { padding ->
        if (notas.isEmpty()) {
            // Estado vacío: guía al usuario cuando todavía no hay notas.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes notas. Toca el ícono de nota para crear la primera.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notas, key = { it.id }) { nota ->
                    NoteCard(
                        note = nota,
                        onClick = { onNoteClick(nota) },
                        onDelete = { viewModel.eliminarNota(nota) },
                        darkTheme = darkTheme
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta individual para cada nota con estilo de "hoja de libreta".
 */
@Composable
fun NoteCard(note: Note, onClick: () -> Unit, onDelete: () -> Unit, darkTheme: Boolean) {
    // Color crema para el fondo de la libreta, adaptado al tema
    val notebookColor = if (darkTheme) Color(0xFF3E3C2A) else Color(0xFFFFF9C4)
    val textColor = if (darkTheme) Color.White else Color.Black
    val secondaryTextColor = if (darkTheme) Color.LightGray else Color.DarkGray
    val lineHighlighter = if (darkTheme) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.6f)
    
    // Color de borde para que resalte en fondos claros
    val borderColor = if (darkTheme) Color.Gray.copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = notebookColor),
        // Se agrega borde y elevación para enmarcar la nota y que no se pierda en el fondo
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        // Dibujamos líneas horizontales para simular una hoja de raya
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val lineHeight = 35.dp.toPx()
                    val numberOfLines = (size.height / lineHeight).toInt()
                    for (i in 1..numberOfLines) {
                        val y = i * lineHeight
                        drawLine(
                            color = lineHighlighter,
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title.ifBlank { "(Sin título)" },
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Muestra la fecha formateada de creación
                    Text(
                        text = note.getFormattedDate(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar nota",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
