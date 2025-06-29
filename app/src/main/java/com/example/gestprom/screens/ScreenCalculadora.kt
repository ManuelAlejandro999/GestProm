package com.example.gestprom.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestprom.ui.theme.AppTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestprom.viewmodels.DataViewModel
import com.example.gestprom.viewmodels.AuthViewModel
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import com.example.gestprom.viewmodels.DataState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCalculadora(
    onAtrasClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    val dataViewModel: DataViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    var calificacionObjetivo by remember { mutableStateOf("6.0") }
    var parcial1 by remember { mutableStateOf("") }
    var parcial2 by remember { mutableStateOf("") }
    var parcial3 by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<String?>(null) }
    var mostrarResultado by remember { mutableStateOf(false) }
    var showSelectDialog by remember { mutableStateOf(false) }
    var selectedSemestre by remember { mutableStateOf<String?>(null) }
    var selectedMateria by remember { mutableStateOf<String?>(null) }
    var expandedSemestre by remember { mutableStateOf(false) }
    var expandedMateria by remember { mutableStateOf(false) }

    val semestresState by dataViewModel.semestresState.collectAsState()

    fun calcularNecesarioOrdinario(): String {
        return try {
            val objetivo = calificacionObjetivo.toDoubleOrNull() ?: 6.0
            val p1 = parcial1.toDoubleOrNull() ?: 0.0
            val p2 = parcial2.toDoubleOrNull() ?: 0.0
            val p3 = parcial3.toDoubleOrNull() ?: 0.0

            // Promedio de parciales (50% del total)
            val promedioParciales = (p1 + p2 + p3) / 3
            val puntajeParciales = promedioParciales * 0.5

            // Calcular lo que necesita en el ordinario (50% del total)
            val necesarioOrdinario = (objetivo - puntajeParciales) / 0.5

            if (necesarioOrdinario > 10) {
                // Calcular la máxima calificación posible con 10 en el ordinario
                val maxima = (puntajeParciales + 10.0 * 0.5)
                return "Imposible alcanzar el objetivo :( La calificación máxima que puedes alcanzar es ${String.format("%.1f", maxima)} con 10 en el ordinario."
            }
            when {
                necesarioOrdinario <= 0 -> "¡Ya tienes la calificación objetivo!"
                else -> String.format("%.1f", necesarioOrdinario)
            }
        } catch (e: Exception) {
            "Error en el cálculo"
        }
    }

    // Efecto para rellenar los campos cuando se cargan las evaluaciones de la materia seleccionada
    val evaluaciones by dataViewModel.evaluaciones.collectAsState()
    LaunchedEffect(selectedMateria, evaluaciones) {
        if (selectedMateria != null && evaluaciones.isNotEmpty()) {
            parcial1 = evaluaciones.find { it.nombre == "Parcial 1" }?.calificacionResultado?.takeIf { it > 0.0 }?.toString() ?: ""
            parcial2 = evaluaciones.find { it.nombre == "Parcial 2" }?.calificacionResultado?.takeIf { it > 0.0 }?.toString() ?: ""
            parcial3 = evaluaciones.find { it.nombre == "Parcial 3" }?.calificacionResultado?.takeIf { it > 0.0 }?.toString() ?: ""
        }
    }

    LaunchedEffect(showSelectDialog) {
        val userUid = currentUser?.uid
        println("[DEBUG] LaunchedEffect: showSelectDialog=$showSelectDialog, currentUser?.uid=$userUid")
        if (showSelectDialog && userUid != null) {
            println("[DEBUG] LaunchedEffect: Cargando semestres para $userUid")
            dataViewModel.loadSemestres(userUid)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "¿Cuánto me falta?",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 20.sp,
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
                actions = {
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Inicio",
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Botón para usar datos de una materia
            Button(
                onClick = {
                    val userUid = currentUser?.uid
                    println("[DEBUG] Botón: currentUser?.uid = $userUid")
                    if (userUid != null) {
                        println("[DEBUG] Botón: Cargando semestres para $userUid")
                        dataViewModel.loadSemestres(userUid)
                    }
                    selectedSemestre = null
                    selectedMateria = null
                    showSelectDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
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
                    text = "Usar datos de una materia",
                    color = AppTheme.colors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Diálogo de selección de semestre y materia
            if (showSelectDialog) {
                val semestres by dataViewModel.semestres.collectAsState()
                val materias by dataViewModel.materias.collectAsState()
                AlertDialog(
                    onDismissRequest = { showSelectDialog = false },
                    containerColor = AppTheme.colors.BackgroundPrimary,
                    title = {
                        Text("Selecciona semestre y materia", color = AppTheme.colors.TextPrimary, fontWeight = FontWeight.Bold)
                    },
                    text = {
                        Column {
                            // Dropdown de semestres
                            if (semestresState is DataState.Loading) {
                                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = AppTheme.colors.ButtonPrimary)
                                }
                            } else if (semestresState is DataState.Error) {
                                val errorMsg = (semestresState as? DataState.Error)?.message ?: "Error desconocido"
                                Text("Error al cargar semestres: $errorMsg", color = MaterialTheme.colorScheme.error)
                            } else if (semestres.isEmpty()) {
                                Text("No hay semestres disponibles", color = AppTheme.colors.TextTertiary)
                            } else {
                                println("[DEBUG] Renderizando dropdown de semestres: ${semestres.map { it.nombre }}")
                                Box {
                                    OutlinedTextField(
                                        value = semestres.find { it.id == selectedSemestre }?.nombre ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Semestre", color = AppTheme.colors.TextPrimary) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = AppTheme.colors.TextPrimary
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                                            focusedLabelColor = AppTheme.colors.ButtonPrimary,
                                            focusedTextColor = AppTheme.colors.TextPrimary,
                                            unfocusedTextColor = AppTheme.colors.TextPrimary
                                        )
                                    )
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .clickable {
                                                println("[DEBUG] Click en Box ancla de semestre")
                                                expandedSemestre = true
                                            }
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedSemestre,
                                    onDismissRequest = {
                                        println("[DEBUG] DropdownMenu cerrado")
                                        expandedSemestre = false
                                    }
                                ) {
                                    semestres.forEach { semestre ->
                                        DropdownMenuItem(
                                            text = { Text(semestre.nombre, color = AppTheme.colors.TextPrimary) },
                                            onClick = {
                                                val userUid = currentUser?.uid
                                                selectedSemestre = semestre.id
                                                expandedSemestre = false
                                                if (userUid != null) {
                                                    dataViewModel.loadMaterias(userUid, semestre.id)
                                                }
                                                selectedMateria = null
                                            }
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            // Dropdown de materias
                            if (selectedSemestre != null && materias.isEmpty()) {
                                Text("No hay materias disponibles", color = AppTheme.colors.TextTertiary)
                            } else if (selectedSemestre != null) {
                                Box {
                                    OutlinedTextField(
                                        value = materias.find { it.id == selectedMateria }?.nombre ?: "",
                                        onValueChange = {},
                                        readOnly = true,
                                        label = { Text("Materia", color = AppTheme.colors.TextPrimary) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = AppTheme.colors.TextPrimary
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = selectedSemestre != null,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                                            focusedLabelColor = AppTheme.colors.ButtonPrimary,
                                            focusedTextColor = AppTheme.colors.TextPrimary,
                                            unfocusedTextColor = AppTheme.colors.TextPrimary
                                        )
                                    )
                                    Box(
                                        Modifier
                                            .matchParentSize()
                                            .clickable {
                                                println("[DEBUG] Click en Box ancla de materia")
                                                expandedMateria = true
                                            }
                                    )
                                }
                                DropdownMenu(
                                    expanded = expandedMateria,
                                    onDismissRequest = {
                                        println("[DEBUG] DropdownMenu de materia cerrado")
                                        expandedMateria = false
                                    }
                                ) {
                                    materias.forEach { materia ->
                                        DropdownMenuItem(
                                            text = { Text(materia.nombre, color = AppTheme.colors.TextPrimary) },
                                            onClick = {
                                                selectedMateria = materia.id
                                                expandedMateria = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val materia = materias.find { it.id == selectedMateria }
                                if (materia != null) {
                                    calificacionObjetivo = materia.calificacionObjetivo.toString()
                                    val userUid = currentUser?.uid
                                    if (userUid != null && selectedSemestre != null && selectedMateria != null) {
                                        dataViewModel.loadEvaluaciones(userUid, selectedSemestre!!, selectedMateria!!)
                                    }
                                }
                                showSelectDialog = false
                            },
                            enabled = selectedMateria != null
                        ) {
                            Text("Usar datos", color = AppTheme.colors.TextPrimary)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSelectDialog = false }) {
                            Text("Cancelar", color = AppTheme.colors.TextPrimary)
                        }
                    }
                )
            }

            // Card principal
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.CardBackground//.copy(alpha = 0.8f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Calificación Objetivo
                    Text(
                        text = "Calificación Objetivo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.TextSecondary
                    )

                    OutlinedTextField(
                        value = calificacionObjetivo,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*$")) && newValue.length <= 4) {
                                val numValue = newValue.toDoubleOrNull()
                                if (numValue == null || numValue <= 10.0) {
                                    calificacionObjetivo = newValue
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            unfocusedBorderColor = AppTheme.colors.TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = AppTheme.colors.TextSecondary,
                            unfocusedTextColor = AppTheme.colors.TextSecondary,
                            cursorColor = AppTheme.colors.ButtonPrimary
                        ),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "6.0",
                                color = AppTheme.colors.TextTertiary.copy(alpha = 0.6f)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Parciales
                    Text(
                        text = "Calificaciones de Parciales",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.TextSecondary
                    )

                    // Parcial 1
                    Text(
                        text = "Parcial 1",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.TextSecondary
                    )
                    OutlinedTextField(
                        value = parcial1,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*$")) && newValue.length <= 4) {
                                val numValue = newValue.toDoubleOrNull()
                                if (numValue == null || numValue <= 10.0) {
                                    parcial1 = newValue
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            unfocusedBorderColor = AppTheme.colors.TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = AppTheme.colors.TextSecondary,
                            unfocusedTextColor = AppTheme.colors.TextSecondary,
                            cursorColor = AppTheme.colors.ButtonPrimary
                        ),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "0.0",
                                color = AppTheme.colors.TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    )

                    // Parcial 2
                    Text(
                        text = "Parcial 2",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.TextSecondary
                    )
                    OutlinedTextField(
                        value = parcial2,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*$")) && newValue.length <= 4) {
                                val numValue = newValue.toDoubleOrNull()
                                if (numValue == null || numValue <= 10.0) {
                                    parcial2 = newValue
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            unfocusedBorderColor = AppTheme.colors.TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = AppTheme.colors.TextSecondary,
                            unfocusedTextColor = AppTheme.colors.TextSecondary,
                            cursorColor = AppTheme.colors.ButtonPrimary
                        ),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "0.0",
                                color = AppTheme.colors.TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    )

                    // Parcial 3
                    Text(
                        text = "Parcial 3",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.TextSecondary
                    )
                    OutlinedTextField(
                        value = parcial3,
                        onValueChange = { newValue ->
                            if (newValue.matches(Regex("^\\d*\\.?\\d*$")) && newValue.length <= 4) {
                                val numValue = newValue.toDoubleOrNull()
                                if (numValue == null || numValue <= 10.0) {
                                    parcial3 = newValue
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            unfocusedBorderColor = AppTheme.colors.TextSecondary.copy(alpha = 0.3f),
                            focusedTextColor = AppTheme.colors.TextSecondary,
                            unfocusedTextColor = AppTheme.colors.TextSecondary,
                            cursorColor = AppTheme.colors.ButtonPrimary
                        ),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "0.0",
                                color = AppTheme.colors.TextSecondary.copy(alpha = 0.6f)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón Calcular
                    Button(
                        onClick = {
                            resultado = calcularNecesarioOrdinario()
                            mostrarResultado = true
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.ButtonPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Calcular",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Resultado
            AnimatedVisibility(
                visible = mostrarResultado && resultado != null,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(500)
                ) + fadeIn()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            resultado?.contains("Ya tienes") == true -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                            resultado?.startsWith("Imposible alcanzar el objetivo :(") == true -> Color(0xFFE57373).copy(alpha = 0.1f)
                            else -> AppTheme.colors.ButtonPrimary.copy(alpha = 0.1f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val mensajeCompleto = when {
                            resultado?.contains("Ya tienes") == true -> "¡Felicidades! Ya tienes la calificación objetivo"
                            resultado?.startsWith("Imposible alcanzar el objetivo :(") == true -> resultado ?: ""
                            resultado?.contains("Imposible") == true -> resultado ?: "Imposible alcanzar el objetivo con estas calificaciones"
                            resultado?.contains("Error") == true -> "Error en el cálculo. Verifica los datos"
                            else -> "Necesitas al menos $resultado en el Ordinario para pasar con $calificacionObjetivo"
                        }

                        Text(
                            text = mensajeCompleto,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                resultado?.contains("Ya tienes") == true -> Color(0xFF2E7D32)
                                resultado?.startsWith("Imposible alcanzar el objetivo :(") == true -> Color(0xFFD32F2F)
                                else -> AppTheme.colors.ButtonPrimary
                            },
                            textAlign = TextAlign.Center,
                            lineHeight = 22.sp
                        )
                    }
                }
            }

            // Información del sistema
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.CardBackground//.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Sistema de Evaluación",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Los 3 parciales valen 50% de la calificación final\n• El examen ordinario vale 50% de la calificación final",
                        fontSize = 12.sp,
                        color = AppTheme.colors.TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenCalculadoraPreview() {
    ScreenCalculadora()
}