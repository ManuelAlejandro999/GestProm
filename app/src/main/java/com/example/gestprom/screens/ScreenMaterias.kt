package com.example.gestprom.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestprom.models.Materia
import com.example.gestprom.ui.theme.AppTheme
import com.example.gestprom.viewmodels.AuthViewModel
import com.example.gestprom.viewmodels.DataState
import com.example.gestprom.viewmodels.DataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMaterias(
    semestreName: String = "",
    semestreId: String = "",
    authViewModel: AuthViewModel = viewModel(),
    dataViewModel: DataViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onAddMateriaClick: () -> Unit = {},
    onConfigurarEvaluacionesClick: () -> Unit = {},
    onMateriaClick: (Materia) -> Unit = {}
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var nombreMateria by remember { mutableStateOf("") }
    var calificacionObjetivo by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val currentUser by authViewModel.currentUser.collectAsState()
    val materias by dataViewModel.materias.collectAsState()
    val materiasState by dataViewModel.materiasState.collectAsState()
    val semestres by dataViewModel.semestres.collectAsState()

    // Load materias when user and semestreId are available
    LaunchedEffect(currentUser, semestreId) {
        if (currentUser != null && semestreId.isNotEmpty()) {
            dataViewModel.loadMaterias(currentUser!!.uid, semestreId)
        }
    }

    // Load semestres to get configuration
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            dataViewModel.loadSemestres(user.uid)
        }
    }

    // Get current semestre configuration
    val semestreActual = semestres.find { it.id == semestreId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (semestreName.isNotEmpty()) semestreName else "Asignaturas",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = AppTheme.colors.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.BackgroundPrimary
                )
            )
        },
        containerColor = AppTheme.colors.BackgroundPrimary
    ) { paddingValues ->

        if (materiasState is DataState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = AppTheme.colors.ButtonPrimary
                )
            }
        } else if (materiasState is DataState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (materiasState as DataState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else if (materias.isEmpty()) {
            // Pantalla vacía
            EmptyStateContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAddMateriaClick = { showAddDialog = true },
                onConfigurarEvaluacionesClick = onConfigurarEvaluacionesClick
            )
        } else {
            // Pantalla con materias
            MateriasContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                materias = materias,
                onAddMateriaClick = { showAddDialog = true },
                onConfigurarEvaluacionesClick = onConfigurarEvaluacionesClick,
                onMateriaClick = onMateriaClick,
                semestreId = semestreId
            )
        }
    }

    // Dialog para añadir materia
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                nombreMateria = ""
                calificacionObjetivo = ""
                errorMessage = ""
            },
            containerColor = AppTheme.colors.BackgroundPrimary,
            title = {
                Text(
                    text = "Añadir Asignatura",
                    color = AppTheme.colors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = nombreMateria,
                        onValueChange = { nombreMateria = it },
                        label = { Text("Nombre de la asignatura", color = AppTheme.colors.TextPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            focusedLabelColor = AppTheme.colors.ButtonPrimary,
                            focusedTextColor = AppTheme.colors.TextPrimary,
                            unfocusedTextColor = AppTheme.colors.TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = calificacionObjetivo,
                        onValueChange = { input ->
                            if (input.isEmpty() || input.matches(Regex("^\\d{0,2}(\\.\\d{0,1})?$"))) {
                                calificacionObjetivo = input
                                val calificacion = input.toDoubleOrNull()
                                errorMessage = when {
                                    input.isEmpty() -> ""
                                    calificacion == null -> "Formato inválido"
                                    calificacion < 1.0 -> "Mínimo 1.0"
                                    calificacion > 10.0 -> "Máximo 10.0"
                                    else -> ""
                                }
                            }
                        },
                        label = { Text("Calificación objetivo (1.0 - 10.0)", color = AppTheme.colors.TextPrimary) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = errorMessage.isNotEmpty(),
                        supportingText = if (errorMessage.isNotEmpty()) {
                            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                        } else null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            focusedLabelColor = AppTheme.colors.ButtonPrimary,
                            focusedTextColor = AppTheme.colors.TextPrimary,
                            unfocusedTextColor = AppTheme.colors.TextPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val calificacion = calificacionObjetivo.toDoubleOrNull()
                        if (nombreMateria.isNotBlank() &&
                            calificacion != null &&
                            calificacion >= 1.0 &&
                            calificacion <= 10.0 &&
                            currentUser != null) {
                            dataViewModel.loadSemestres(currentUser!!.uid)
                            val semestreActualizado = dataViewModel.semestres.value.find { it.id == semestreId }
                            val nuevaMateria = Materia(
                                nombre = nombreMateria,
                                calificacionObjetivo = calificacion
                            )
                            dataViewModel.createMateriaWithEvaluaciones(
                                currentUser!!.uid,
                                semestreId,
                                nuevaMateria,
                                semestreActualizado?.configuracionEvaluaciones
                            )
                            showAddDialog = false
                            nombreMateria = ""
                            calificacionObjetivo = ""
                            errorMessage = ""
                        }
                    },
                    enabled = nombreMateria.isNotBlank() &&
                            calificacionObjetivo.isNotBlank() &&
                            errorMessage.isEmpty() &&
                            calificacionObjetivo.toDoubleOrNull()?.let { it >= 1.0 && it <= 10.0 } == true
                ) {
                    Text("Guardar", color = AppTheme.colors.TextPrimary)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddDialog = false
                        nombreMateria = ""
                        calificacionObjetivo = ""
                        errorMessage = ""
                    }
                ) {
                    Text("Cancelar", color = AppTheme.colors.TextPrimary)
                }
            }
        )
    }
}

@Composable
private fun EmptyStateContent(
    modifier: Modifier = Modifier,
    onAddMateriaClick: () -> Unit,
    onConfigurarEvaluacionesClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Texto informativo
        Text(
            text = "No tienes asignaturas registradas",
            fontSize = 18.sp,
            color = AppTheme.colors.TextPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón Agregar asignatura
        Button(
            onClick = onAddMateriaClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.ButtonPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppTheme.colors.TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Agregar asignatura",
                color = AppTheme.colors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Configurar Evaluaciones
        Button(
            onClick = onConfigurarEvaluacionesClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.ButtonPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Configurar Evaluaciones",
                color = AppTheme.colors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MateriasContent(
    modifier: Modifier = Modifier,
    materias: List<Materia>,
    onAddMateriaClick: () -> Unit,
    onConfigurarEvaluacionesClick: () -> Unit,
    onMateriaClick: (Materia) -> Unit,
    semestreId: String
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editingMateria by remember { mutableStateOf<Materia?>(null) }
    var tempNombre by remember { mutableStateOf("") }
    val dataViewModel: DataViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = modifier.padding(24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(materias) { materia: Materia ->
                MateriaCard(
                    materia = materia,
                    onClick = { onMateriaClick(materia) },
                    onRename = {
                        editingMateria = it
                        tempNombre = it.nombre
                        showRenameDialog = true
                    },
                    onDelete = {
                        editingMateria = it
                        showDeleteDialog = true
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Agregar asignatura
        Button(
            onClick = onAddMateriaClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.ButtonPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = AppTheme.colors.TextPrimary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Agregar asignatura",
                color = AppTheme.colors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Configurar Evaluaciones
        Button(
            onClick = onConfigurarEvaluacionesClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppTheme.colors.ButtonPrimary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Configurar Evaluaciones",
                color = AppTheme.colors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
    
    // Diálogo para renombrar materia
    if (showRenameDialog && editingMateria != null) {
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                editingMateria = null
                tempNombre = ""
            },
            containerColor = AppTheme.colors.BackgroundPrimary,
            title = {
                Text(
                    text = "Renombrar materia",
                    color = AppTheme.colors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempNombre,
                    onValueChange = { tempNombre = it },
                    label = { Text("Nuevo nombre", color = AppTheme.colors.TextPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.ButtonPrimary,
                        focusedLabelColor = AppTheme.colors.ButtonPrimary,
                        focusedTextColor = AppTheme.colors.TextPrimary,
                        unfocusedTextColor = AppTheme.colors.TextPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    println("DEBUG: PRESIONADO Guardar en dialogo de materia")
                    println("DEBUG: tempNombre='$tempNombre', currentUser=${currentUser?.uid}, semestreId='$semestreId', editingMateria=$editingMateria")
                    if (tempNombre.isNotBlank() && currentUser != null && semestreId.isNotEmpty() && editingMateria != null) {
                        val materiaActualizada = editingMateria!!.copy(nombre = tempNombre)
                        dataViewModel.updateMateria(currentUser?.uid ?: "", semestreId, materiaActualizada)
                        showRenameDialog = false
                        editingMateria = null
                        tempNombre = ""
                    }
                }) {
                    Text("Guardar", color = AppTheme.colors.TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    editingMateria = null
                    tempNombre = ""
                }) {
                    Text("Cancelar", color = AppTheme.colors.TextPrimary)
                }
            }
        )
    }
    
    // Diálogo para eliminar materia
    if (showDeleteDialog && editingMateria != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppTheme.colors.BackgroundPrimary,
            title = {
                Text(
                    text = "Eliminar materia",
                    color = AppTheme.colors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar la materia '${editingMateria?.nombre}'? Esta acción no se puede deshacer.",
                    color = AppTheme.colors.TextPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    println("DEBUG: PRESIONADO Eliminar en dialogo de materia")
                    println("DEBUG: currentUser=${currentUser?.uid}, semestreId='$semestreId', editingMateria=$editingMateria")
                    if (currentUser != null && semestreId.isNotEmpty() && editingMateria != null) {
                        dataViewModel.deleteMateria(currentUser?.uid ?: "", semestreId, editingMateria!!.id)
                    }
                    showDeleteDialog = false
                    editingMateria = null
                }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = AppTheme.colors.TextPrimary)
                }
            }
        )
    }
}

@Composable
private fun MateriaCard(
    materia: Materia,
    onClick: () -> Unit = {},
    onRename: (Materia) -> Unit = {},
    onDelete: (Materia) -> Unit = {}
) {
    var expandedMenu by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Nombre de la materia
                Text(
                    text = materia.nombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Calificación Objetivo",
                    fontSize = 14.sp,
                    color = AppTheme.colors.TextTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.ButtonPrimary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        2.dp,
                        AppTheme.colors.ButtonPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${materia.calificacionObjetivo}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.TextSecondary
                        )
                    }
                }
            }
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Opciones",
                        tint = AppTheme.colors.TextPrimary
                    )
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Renombrar") },
                        onClick = {
                            expandedMenu = false
                            onRename(materia)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = {
                            expandedMenu = false
                            onDelete(materia)
                        }
                    )
                }
            }
        }
    }
}