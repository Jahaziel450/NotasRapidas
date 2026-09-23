package com.example.notasrapidas

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

/**
 * Pantalla para crear una nota nueva o editar una existente.
 * Si "existingNote" es null, es una nota nueva; si no, se están editando sus campos.
 * Incluye un diseño visual de "papel de libreta" donde el texto se alinea con las líneas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    existingNote: Note?,
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    darkTheme: Boolean
) {
    // remember + mutableStateOf: guarda el texto que el usuario va escribiendo
    var titulo by remember { mutableStateOf(existingNote?.title ?: "") }
    var contenido by remember { mutableStateOf(existingNote?.content ?: "") }

    // Color crema para el fondo del papel, se ajusta si está en modo oscuro
    val notebookColor = if (darkTheme) Color(0xFF3E3C2A) else Color(0xFFFFF9C4)
    val textColor = if (darkTheme) Color.White else Color.Black
    val lineHighlighter = if (darkTheme) Color.Gray.copy(alpha = 0.3f) else Color.LightGray.copy(alpha = 0.5f)
    
    // Definimos una altura de línea fija para que el texto coincida con el dibujo de las rayas
    val customLineHeight = 32.sp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingNote == null) "Nueva nota" else "Editar nota") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.guardarNota(
                            id = existingNote?.id ?: 0,
                            titulo = titulo,
                            contenido = contenido
                        )
                        onBack()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "Guardar nota")
                    }
                }
            )
        }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = notebookColor
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // Dibujamos líneas para simular el papel de libreta
                        // La distancia entre líneas coincide con la altura del texto (customLineHeight)
                        val lineHeightPx = customLineHeight.toPx()
                        val numberOfLines = (size.height / lineHeightPx).toInt()
                        for (i in 1..numberOfLines) {
                            val y = i * lineHeightPx
                            drawLine(
                                color = lineHighlighter,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                    .padding(16.dp)
            ) {
                // OutlinedTextField proporciona el "contorno" solicitado para ubicar mejor el campo
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título de la nota") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    textStyle = MaterialTheme.typography.titleLarge.copy(lineHeight = customLineHeight),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Campo de contenido con contorno y alineación de texto a las líneas del cuaderno
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido de la nota") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    // Forzamos que la altura de línea sea exacta para que el texto "se siente" sobre las rayas
                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = customLineHeight)
                )
            }
        }
    }
}
