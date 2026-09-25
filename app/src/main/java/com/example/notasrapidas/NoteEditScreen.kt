package com.example.notasrapidas

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

/**
 * Pantalla para crear/editar una nota con:
 * - Selección de imagen usando ActivityResultContracts.GetContent().
 * - Vista previa interactiva de la imagen con toque para Zoom.
 * - Botones táctiles grandes y accesibles.
 * - Papel de libreta con líneas alineadas al texto en modo claro u oscuro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditScreen(
    existingNote: Note?,
    viewModel: NoteViewModel,
    onBack: () -> Unit,
    darkTheme: Boolean
) {
    var titulo by remember { mutableStateOf(existingNote?.title ?: "") }
    var contenido by remember { mutableStateOf(existingNote?.content ?: "") }
    var imageUri by remember { mutableStateOf(existingNote?.imageUri) }
    var showZoomViewer by remember { mutableStateOf(false) }

    // Launcher para seleccionar imagen de la galería de forma nativa
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { imageUri = it.toString() }
    }

    val notebookColor = if (darkTheme) Color(0xFF2C2B22) else Color(0xFFFFF9C4)
    val textColor = if (darkTheme) Color.White else Color.Black
    val lineHighlighter = if (darkTheme) Color.Gray.copy(alpha = 0.25f) else Color.LightGray.copy(alpha = 0.5f)
    val customLineHeight = 34.sp

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existingNote == null) "Nueva nota" else "Editar nota") },
                navigationIcon = {
                    // Botón grande para volver
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    // Botón grande para seleccionar/cambiar imagen
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Agregar o cambiar imagen",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Botón grande para guardar la nota
                    IconButton(
                        onClick = {
                            viewModel.guardarNota(
                                id = existingNote?.id ?: 0,
                                titulo = titulo,
                                contenido = contenido,
                                imageUri = imageUri,
                                isPinned = existingNote?.isPinned ?: false,
                                orderPosition = existingNote?.orderPosition ?: 0
                            )
                            onBack()
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar nota",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
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
                    .verticalScroll(rememberScrollState())
                    .drawBehind {
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
                // Campo Título
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
                    textStyle = MaterialTheme.typography.titleLarge.copy(lineHeight = customLineHeight, fontSize = 20.sp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Botón grande para seleccionar imagen si aún no se ha adjuntado una
                if (imageUri == null) {
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Adjuntar Imagen a la nota",
                            fontSize = 17.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // Vista previa de la imagen adjunta con controles
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp))
                            .clickable { showZoomViewer = true }
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Imagen adjunta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Etiqueta indicadora de Zoom
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(10.dp)
                        ) {
                            Text(
                                text = "Toca para hacer Zoom",
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Botón grande para quitar la imagen
                        IconButton(
                            onClick = { imageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(48.dp)
                        ) {
                            Surface(
                                color = Color.Red.copy(alpha = 0.85f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar imagen",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Campo Contenido
                OutlinedTextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    label = { Text("Contenido de la nota") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(lineHeight = customLineHeight, fontSize = 16.sp)
                )
            }
        }

        // Visor Zoomable si se toca la imagen
        if (showZoomViewer && imageUri != null) {
            ZoomableImageViewer(
                imageUri = imageUri!!,
                onClose = { showZoomViewer = false }
            )
        }
    }
}
