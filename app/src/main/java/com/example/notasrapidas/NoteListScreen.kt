package com.example.notasrapidas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage

/**
 * Pantalla principal con:
 * - Gesto de deslizar a la DERECHA para Anclar y Desanclar fluido.
 * - Deslizar a la IZQUIERDA para Eliminar con confirmación.
 * - Ocultamiento automático del botón FAB "+" al hacer scroll hacia abajo.
 * - Sin botón duplicado de anclaje (se usa exclusivamente el gesto).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NoteListScreen(
    viewModel: NoteViewModel,
    onNoteClick: (Note) -> Unit,
    onAddNote: () -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val notas by viewModel.notes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var selectedImageForZoom by remember { mutableStateOf<String?>(null) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    // Estado de la lista para detectar el scroll
    val listState = rememberLazyListState()

    // Ocultar FAB cuando se hace scroll hacia abajo
    var isFabVisible by remember { mutableStateOf(true) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
            .collect { (currentIndex, currentOffset) ->
                if (currentIndex > previousIndex || (currentIndex == previousIndex && currentOffset > previousScrollOffset + 10)) {
                    // Haciendo scroll HACIA ABAJO -> Esconder FAB
                    isFabVisible = false
                } else if (currentIndex < previousIndex || currentOffset < previousScrollOffset - 10) {
                    // Haciendo scroll HACIA ARRIBA -> Mostrar FAB
                    isFabVisible = true
                }
                previousIndex = currentIndex
                previousScrollOffset = currentOffset
            }
    }

    // Estado del arrastrado de reordenamiento
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Buscar notas...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { viewModel.onSearchQueryChange("") },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar búsqueda",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                            )
                        )
                    } else {
                        Text(
                            text = "Notas Rápidas",
                            style = MaterialTheme.typography.titleLarge.copy(fontSize = 22.sp)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            isSearchActive = !isSearchActive
                            if (!isSearchActive) {
                                viewModel.onSearchQueryChange("")
                            }
                        },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Modo Búsqueda",
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkTheme) "Modo Claro" else "Modo Oscuro",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Animación suave de aparición/desaparición del FAB al hacer scroll
            AnimatedVisibility(
                visible = isFabVisible,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                FloatingActionButton(
                    onClick = onAddNote,
                    modifier = Modifier.size(72.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar nota",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (notas.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotEmpty()) {
                            "No se encontraron notas con '$searchQuery'"
                        } else {
                            "Aún no tienes notas. ¡Toca el botón '+' para crear una!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    itemsIndexed(
                        items = notas,
                        key = { _, nota -> nota.id }
                    ) { index, nota ->
                        val isDraggingThisItem = draggedIndex == index

                        val dismissState = rememberSwipeToDismissBoxState(
                            positionalThreshold = { totalDistance -> totalDistance * 0.35f },
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    noteToDelete = nota
                                    false
                                } else {
                                    true
                                }
                            }
                        )

                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd) {
                                viewModel.togglePinNota(nota)
                                dismissState.reset()
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val direction = dismissState.dismissDirection
                                if (direction != SwipeToDismissBoxValue.Settled) {
                                    val isPinAction = direction == SwipeToDismissBoxValue.StartToEnd
                                    val bgColor = if (isPinAction) Color(0xFF1976D2) else Color(0xFFE53935)
                                    val alignment = if (isPinAction) Alignment.CenterStart else Alignment.CenterEnd
                                    val icon = if (isPinAction) Icons.Default.PushPin else Icons.Default.Delete
                                    val text = if (isPinAction) {
                                        if (nota.isPinned) "Desanclar" else "Anclar"
                                    } else "Eliminar"

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(bgColor)
                                            .padding(horizontal = 24.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (isPinAction) {
                                                Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(32.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                            } else {
                                                Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(icon, contentDescription = text, tint = Color.White, modifier = Modifier.size(32.dp))
                                            }
                                        }
                                    }
                                }
                            },
                            content = {
                                NoteCard(
                                    note = nota,
                                    onClick = { onNoteClick(nota) },
                                    onImageClick = { imageUri -> selectedImageForZoom = imageUri },
                                    darkTheme = darkTheme,
                                    isDragging = isDraggingThisItem,
                                    dragOffsetY = if (isDraggingThisItem) dragOffsetY else 0f,
                                    dragHandleModifier = Modifier.pointerInput(notas.size) {
                                        detectDragGestures(
                                            onDragStart = {
                                                draggedIndex = index
                                                dragOffsetY = 0f
                                            },
                                            onDrag = { change, dragAmount ->
                                                change.consume()
                                                dragOffsetY += dragAmount.y
                                                val currentIdx = draggedIndex ?: return@detectDragGestures
                                                val cardEstimatedHeightPx = 135.dp.toPx()
                                                val steps = (dragOffsetY / cardEstimatedHeightPx).toInt()
                                                if (steps != 0) {
                                                    val targetIndex = (currentIdx + steps).coerceIn(0, notas.size - 1)
                                                    if (targetIndex != currentIdx) {
                                                        viewModel.moverNota(currentIdx, targetIndex)
                                                        draggedIndex = targetIndex
                                                        dragOffsetY = 0f
                                                    }
                                                }
                                            },
                                            onDragEnd = {
                                                draggedIndex = null
                                                dragOffsetY = 0f
                                            },
                                            onDragCancel = {
                                                draggedIndex = null
                                                dragOffsetY = 0f
                                            }
                                        )
                                    },
                                    modifier = Modifier.animateItemPlacement()
                                )
                            }
                        )
                    }
                }
            }
        }

        // Diálogo de confirmación para eliminar nota
        noteToDelete?.let { nota ->
            AlertDialog(
                onDismissRequest = { noteToDelete = null },
                title = { Text("Eliminar nota", style = MaterialTheme.typography.titleLarge) },
                text = { Text("¿Estás seguro de que deseas eliminar la nota '${nota.title.ifBlank { "Sin título" }}'?", fontSize = 16.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.eliminarNota(nota)
                            noteToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Eliminar", color = Color.White, fontSize = 16.sp)
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { noteToDelete = null }) {
                        Text("Cancelar", fontSize = 16.sp)
                    }
                }
            )
        }

        // Visor de Imágenes con Gesto de Zoom (Pinch to zoom)
        selectedImageForZoom?.let { imageUri ->
            ZoomableImageViewer(
                imageUri = imageUri,
                onClose = { selectedImageForZoom = null }
            )
        }
    }
}

/**
 * Tarjeta individual de nota con diseño limpio de libreta, sin botones duplicados.
 * El anclaje se realiza exclusivamente con el gesto de deslizar a la derecha.
 */
@Composable
fun NoteCard(
    note: Note,
    onClick: () -> Unit,
    onImageClick: (String) -> Unit,
    darkTheme: Boolean,
    isDragging: Boolean = false,
    dragOffsetY: Float = 0f,
    dragHandleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    val animatedScale by animateFloatAsState(targetValue = if (isDragging) 1.05f else 1.0f, label = "ScaleAnimation")
    
    val notebookColor = if (darkTheme) Color(0xFF2C2B22) else Color(0xFFFFF9C4)
    val textColor = if (darkTheme) Color(0xFFEEEEEE) else Color(0xFF1C1B1F)
    val secondaryTextColor = if (darkTheme) Color(0xFFB0BEC5) else Color(0xFF49454F)
    val lineHighlighter = if (darkTheme) Color.Gray.copy(alpha = 0.25f) else Color.LightGray.copy(alpha = 0.6f)
    
    val borderColor = when {
        isDragging -> MaterialTheme.colorScheme.primary
        note.isPinned -> MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
        darkTheme -> Color.Gray.copy(alpha = 0.4f)
        else -> Color.Gray.copy(alpha = 0.3f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer(
                scaleX = animatedScale,
                scaleY = animatedScale,
                translationY = dragOffsetY,
                shadowElevation = if (isDragging) 24f else 4f
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = notebookColor),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isDragging || note.isPinned) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDragging) 16.dp else if (note.isPinned) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    val lineHeight = 36.dp.toPx()
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
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = note.title.ifBlank { "(Sin título)" },
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 19.sp, fontWeight = FontWeight.Bold),
                        color = textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Badge indicador cuando la nota está anclada
                        if (note.isPinned) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Nota Anclada",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Anclada",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Manija de agarre para arrastrar
                        Box(
                            modifier = dragHandleModifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent)
                                .padding(6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Arrastrar para mover de orden",
                                tint = if (isDragging) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.8f),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                if (note.content.isNotBlank()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        color = secondaryTextColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Miniatura de imagen si existe
                note.imageUri?.let { uri ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 140.dp, height = 90.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                            .clickable { onImageClick(uri) }
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Imagen adjunta",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = note.getFormattedDate(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (darkTheme) Color.LightGray else Color.Gray
                )
            }
        }
    }
}

/**
 * Visor de imágenes en pantalla completa con gestos multitáctiles.
 */
@Composable
fun ZoomableImageViewer(
    imageUri: String,
    onClose: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onClose,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.92f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            val maxX = (scale - 1) * size.width / 2f
                            val maxY = (scale - 1) * size.height / 2f
                            offset = Offset(
                                x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Imagen con Zoom",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    contentScale = ContentScale.Fit
                )
            }

            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp)
                    .size(56.dp)
                    .background(Color.Black.copy(alpha = 0.6f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar visor de imagen",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
