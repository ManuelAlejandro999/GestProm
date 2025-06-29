package com.example.gestprom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import com.example.gestprom.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestprom.models.Semestre
import com.example.gestprom.ui.theme.AppTheme
import com.example.gestprom.viewmodels.AuthViewModel
import com.example.gestprom.viewmodels.DataState
import com.example.gestprom.viewmodels.DataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSemestres(
    authViewModel: AuthViewModel = viewModel(),
    dataViewModel: DataViewModel = viewModel(),
    onAtrasClick: () -> Unit = {},
    onSemestreClick: (String) -> Unit = {}
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editingSemestre by remember { mutableStateOf<Semestre?>(null) }
    var tempNombre by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newSemestreNombre by remember { mutableStateOf("") }
    var expandedMenuIndex by remember { mutableStateOf(-1) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val semestres by dataViewModel.semestres.collectAsState()
    val semestresState by dataViewModel.semestresState.collectAsState()

    // Load semestres when user is available
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            dataViewModel.loadSemestres(user.uid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Semestres",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onAtrasClick) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Botón para añadir semestres
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = semestresState !is DataState.Loading
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppTheme.colors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Añadir Semestre",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Loading state
            if (semestresState is DataState.Loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = AppTheme.colors.ButtonPrimary
                    )
                }
            }
            // Error state
            else if (semestresState is DataState.Error) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (semestresState as DataState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
            // Success state
            else {
                // Lista de semestres
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(semestres) { index, semestre ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón del semestre
                            Button(
                                onClick = { onSemestreClick(semestre.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AppTheme.colors.CardBackground
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = semestre.nombre,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AppTheme.colors.TextSecondary
                                )
                            }

                            // Menú de opciones
                            Box {
                                IconButton(
                                    onClick = { expandedMenuIndex = index },
                                    modifier = Modifier
                                        .size(56.dp)
                                        .background(
                                            AppTheme.colors.TextTertiary.copy(alpha = 0.6f),
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Opciones",
                                        tint = AppTheme.colors.TextPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedMenuIndex == index,
                                    onDismissRequest = { expandedMenuIndex = -1 }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Renombrar") },
                                        onClick = {
                                            editingSemestre = semestre
                                            tempNombre = semestre.nombre
                                            showEditDialog = true
                                            expandedMenuIndex = -1
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Eliminar") },
                                        onClick = {
                                            editingSemestre = semestre
                                            expandedMenuIndex = -1
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog para añadir semestre
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = AppTheme.colors.BackgroundPrimary,
            title = {
                Text(
                    text = "Añadir Semestre",
                    color = AppTheme.colors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = newSemestreNombre,
                    onValueChange = { newSemestreNombre = it },
                    label = { Text("Nombre del semestre", color = AppTheme.colors.TextPrimary) },
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
                TextButton(
                    onClick = {
                        if (newSemestreNombre.isNotBlank() && currentUser != null) {
                            val newSemestre = Semestre(nombre = newSemestreNombre)
                            dataViewModel.createSemestre(currentUser!!.uid, newSemestre)
                            newSemestreNombre = ""
                            showAddDialog = false
                        }
                    },
                    enabled = newSemestreNombre.isNotBlank()
                ) {
                    Text("Añadir", color = AppTheme.colors.TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancelar", color = AppTheme.colors.TextPrimary)
                }
            }
        )
    }

    // Dialog para editar nombre
    if (showEditDialog && editingSemestre != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = AppTheme.colors.BackgroundPrimary,
            title = {
                Text(
                    text = "Editar Nombre",
                    color = AppTheme.colors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                OutlinedTextField(
                    value = tempNombre,
                    onValueChange = { tempNombre = it },
                    label = { Text("Nombre del semestre", color = AppTheme.colors.TextPrimary) },
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
                    if (tempNombre.isNotBlank() && currentUser != null && editingSemestre != null) {
                        val updatedSemestre = editingSemestre!!.copy(nombre = tempNombre)
                        dataViewModel.updateSemestre(currentUser!!.uid, updatedSemestre)
                        showEditDialog = false
                    }
                }) {
                    Text("Guardar", color = AppTheme.colors.TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancelar", color = AppTheme.colors.TextPrimary)
                }
            }
        )
    }

    // Diálogo para eliminar semestre
    if (showDeleteDialog && editingSemestre != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = AppTheme.colors.BackgroundPrimary,
            title = {
                Text(
                    text = "Eliminar semestre",
                    color = AppTheme.colors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas eliminar el semestre '${editingSemestre?.nombre}'? Esta acción no se puede deshacer.",
                    color = AppTheme.colors.TextPrimary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    currentUser?.let { user ->
                        dataViewModel.deleteSemestre(user.uid, editingSemestre!!.id)
                    }
                    showDeleteDialog = false
                    editingSemestre = null
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