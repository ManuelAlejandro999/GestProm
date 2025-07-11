package com.example.gestprom.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestprom.models.Evaluacion
import com.example.gestprom.models.Materia
import com.example.gestprom.ui.theme.AppTheme
import com.example.gestprom.viewmodels.AuthViewModel
import com.example.gestprom.viewmodels.DataState
import com.example.gestprom.viewmodels.DataViewModel
import com.example.gestprom.viewmodels.EvaluacionPendiente
import com.example.gestprom.viewmodels.TipoPendiente
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEvaluaciones(
    materia: Materia,
    semestreId: String = "",
    authViewModel: AuthViewModel = viewModel(),
    dataViewModel: DataViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    var showEvaluationDialog by remember { mutableStateOf(false) }
    var selectedEvaluacion by remember { mutableStateOf<Evaluacion?>(null) }

    val currentUser by authViewModel.currentUser.collectAsState()
    val evaluaciones by dataViewModel.evaluaciones.collectAsState()
    val evaluacionesState by dataViewModel.evaluacionesState.collectAsState()

    // Load evaluaciones when user, semestreId and materiaId are available
    LaunchedEffect(currentUser, semestreId, materia.id) {
        if (currentUser != null && semestreId.isNotEmpty() && materia.id.isNotEmpty()) {
            dataViewModel.loadEvaluaciones(currentUser!!.uid, semestreId, materia.id)
        }
    }

    // Crear evaluaciones por defecto si no existen
    val evaluacionesPorDefecto = remember {
        listOf(
            Evaluacion(nombre = "Parcial 1", fecha = "15/02/2026"),
            Evaluacion(nombre = "Parcial 2", fecha = "15/03/2026"),
            Evaluacion(nombre = "Parcial 3", fecha = "15/04/2026"),
            Evaluacion(nombre = "Ordinario", fecha = "25/04/2026")
        )
    }

    // Combinar evaluaciones existentes con las por defecto
    val evaluacionesCompletas = remember(evaluaciones) {
        val evaluacionesExistentes = evaluaciones.toMutableList()
        evaluacionesPorDefecto.forEach { evalDefecto ->
            if (!evaluacionesExistentes.any { it.nombre == evalDefecto.nombre }) {
                evaluacionesExistentes.add(evalDefecto)
            }
        }
        evaluacionesExistentes.sortedBy { it.nombre }
    }

    // Obtener evaluaciones pendientes
    val evaluacionesPendientes = remember(currentUser, semestreId, materia.id, evaluaciones) {
        if (currentUser != null && semestreId.isNotEmpty() && materia.id.isNotEmpty()) {
            dataViewModel.validarEvaluacionesPendientes(currentUser!!.uid, semestreId, materia.id)
        } else {
            emptyList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = materia.nombre,
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

        if (evaluacionesState is DataState.Loading) {
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
        } else if (evaluacionesState is DataState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (evaluacionesState as DataState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
            ) {
                // Banner de alertas para evaluaciones pendientes
                if (evaluacionesPendientes.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                evaluacionesPendientes.any { it.tipo == TipoPendiente.FECHA_VENCIDA } -> 
                                    Color(0xFFFFEBEE) // Rojo claro
                                evaluacionesPendientes.any { it.tipo == TipoPendiente.HOY } -> 
                                    Color(0xFFFFF3E0) // Naranja claro
                                else -> Color(0xFFE3F2FD) // Azul claro
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                evaluacionesPendientes.any { it.tipo == TipoPendiente.FECHA_VENCIDA } -> 
                                    Color(0xFFD32F2F) // Rojo
                                evaluacionesPendientes.any { it.tipo == TipoPendiente.HOY } -> 
                                    Color(0xFFFF9800) // Naranja
                                else -> Color(0xFF2196F3) // Azul
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = when {
                                    evaluacionesPendientes.any { it.tipo == TipoPendiente.FECHA_VENCIDA } -> 
                                        "⚠️ Evaluaciones Pendientes"
                                    evaluacionesPendientes.any { it.tipo == TipoPendiente.HOY } -> 
                                        "📅 Evaluación Hoy"
                                    else -> "📋 Evaluaciones Futuras"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.TextSecondary
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            evaluacionesPendientes.forEach { pendiente ->
                                Text(
                                    text = "• ${pendiente.mensaje}",
                                    fontSize = 14.sp,
                                    color = AppTheme.colors.TextSecondary,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Lista de evaluaciones
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(evaluacionesCompletas) { evaluacion: Evaluacion ->
                        EvaluacionCard(
                            evaluacion = evaluacion,
                            materia = materia,
                            dataViewModel = dataViewModel,
                            evaluacionesPendientes = evaluacionesPendientes,
                            onClick = {
                                selectedEvaluacion = evaluacion
                                showEvaluationDialog = true
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón Agregar Resultado
                Button(
                    onClick = {
                        // Seleccionar la primera evaluación sin calificación por defecto
                        val evaluacionPendiente = evaluacionesCompletas.firstOrNull { it.calificacionResultado == 0.0 }
                        selectedEvaluacion = evaluacionPendiente ?: evaluacionesCompletas.first()
                        showEvaluationDialog = true
                    },
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
                        text = "Agregar Resultado",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }

    // Dialog para editar calificación de evaluación
    if (showEvaluationDialog && selectedEvaluacion != null) {
        EditEvaluacionDialog(
            evaluacion = selectedEvaluacion!!,
            evaluaciones = evaluacionesCompletas,
            currentUser = currentUser,
            semestreId = semestreId,
            materiaId = materia.id,
            dataViewModel = dataViewModel,
            onDismiss = {
                showEvaluationDialog = false
                selectedEvaluacion = null
            },
            onConfirm = { evaluacionId, calificacion ->
                if (currentUser != null) {
                    // Buscar la evaluación correcta por id
                    val evaluacionAActualizar = evaluacionesCompletas.find { it.id == evaluacionId } ?: return@EditEvaluacionDialog
                    val evaluacionActualizada = evaluacionAActualizar.copy(
                        calificacionResultado = calificacion
                    )
                    dataViewModel.updateEvaluacion(
                        currentUser!!.uid,
                        semestreId,
                        materia.id,
                        evaluacionActualizada
                    )
                }
                showEvaluationDialog = false
                selectedEvaluacion = null
            }
        )
    }
}

@Composable
private fun EvaluacionCard(
    evaluacion: Evaluacion,
    materia: Materia,
    dataViewModel: DataViewModel,
    evaluacionesPendientes: List<EvaluacionPendiente>,
    onClick: () -> Unit
) {
    val fechaEvaluacion = evaluacion.fecha
    val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val fechaEvaluacionDate = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaEvaluacion)
    } catch (e: Exception) {
        null
    }
    val fechaActualDate = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaActual)
    } catch (e: Exception) {
        null
    }
    
    val fechaCumplida = fechaEvaluacionDate != null && fechaActualDate != null && 
                       (fechaEvaluacionDate?.before(fechaActualDate) == true || fechaEvaluacionDate?.equals(fechaActualDate) == true)
    val tieneCalificacion = evaluacion.calificacionResultado > 0.0
    
    // Buscar si esta evaluación está pendiente
    val evaluacionPendiente = evaluacionesPendientes.find { it.evaluacion.id == evaluacion.id }
    
    // Calcular calificación necesaria si no tiene calificación
    val calificacionNecesaria = if (!tieneCalificacion) {
        dataViewModel.calcularCalificacionNecesaria(evaluacion, materia)
    } else 0.0

    // Calcular máxima calificación alcanzable si la necesaria supera 10.0
    val maximaCalificacionAlcanzable = if (!tieneCalificacion && calificacionNecesaria > 10.0) {
        dataViewModel.calcularMaximaCalificacionAlcanzable(evaluacion, materia)
    } else 0.0

    // Determinar si se puede alcanzar el objetivo
    val puedeAlcanzarObjetivo = if (!tieneCalificacion) {
        dataViewModel.sePuedeAlcanzarObjetivo(evaluacion, materia)
    } else true

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                evaluacionPendiente?.tipo == TipoPendiente.FECHA_VENCIDA -> 
                    Color(0xFFFFEBEE) // Rojo claro para fechas vencidas
                evaluacionPendiente?.tipo == TipoPendiente.HOY -> 
                    Color(0xFFFFF3E0) // Naranja claro para hoy
                else -> AppTheme.colors.CardBackground
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = if (evaluacionPendiente != null) {
            androidx.compose.foundation.BorderStroke(
                2.dp,
                when {
                    evaluacionPendiente.tipo == TipoPendiente.FECHA_VENCIDA -> Color(0xFFD32F2F) // Rojo
                    evaluacionPendiente.tipo == TipoPendiente.HOY -> Color(0xFFFF9800) // Naranja
                    else -> Color(0xFF2196F3) // Azul
                }
            )
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = evaluacion.nombre,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Mostrar fecha y estado
                    if (!tieneCalificacion) {
                        Text(
                            text = evaluacion.fecha,
                            fontSize = 14.sp,
                            color = if (fechaCumplida) AppTheme.colors.ButtonPrimary else AppTheme.colors.TextTertiary
                        )
                        if (fechaCumplida) {
                            Text(
                                text = "Fecha cumplida - Puedes agregar resultado",
                                fontSize = 12.sp,
                                color = AppTheme.colors.ButtonPrimary
                            )
                        } else {
                            Text(
                                text = "Fecha pendiente",
                                fontSize = 12.sp,
                                color = AppTheme.colors.TextTertiary
                            )
                        }
                    } else {
                        Text(
                            text = "Calificación registrada",
                            fontSize = 14.sp,
                            color = AppTheme.colors.ButtonPrimary
                        )
                    }
                }

                // Mostrar calificación o "pendiente"
                if (tieneCalificacion) {
                    Card(
                        modifier = Modifier
                            .width(60.dp)
                            .height(40.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = AppTheme.colors.ButtonPrimary.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            AppTheme.colors.ButtonPrimary.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${evaluacion.calificacionResultado}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.TextSecondary
                            )
                        }
                    }
                } else {
                    if (fechaCumplida) {
                        Button(
                            onClick = onClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.ButtonPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Agregar resultado",
                                tint = AppTheme.colors.TextPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Agregar",
                                color = AppTheme.colors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        Text(
                            text = "Pendiente",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = AppTheme.colors.TextTertiary
                        )
                    }
                }
            }
            
            // Mostrar calificación necesaria si no tiene calificación
            if (!tieneCalificacion && calificacionNecesaria > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (puedeAlcanzarObjetivo) Color(0xFFE8F5E8) else Color(0xFFFFF3E0) // Verde claro o naranja claro
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (puedeAlcanzarObjetivo) Color(0xFF4CAF50) else Color(0xFFFF9800) // Verde o naranja
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (puedeAlcanzarObjetivo) "🎯" else "⚠️",
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                if (puedeAlcanzarObjetivo) {
                                    Text(
                                        text = "Calificación necesaria:",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.TextTertiary
                                    )
                                    Text(
                                        text = "${String.format("%.1f", calificacionNecesaria)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32) // Verde oscuro
                                    )
                                } else {
                                    Text(
                                        text = "No se puede alcanzar el objetivo",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.TextTertiary
                                    )
                                    Text(
                                        text = "Máxima calificación posible: ${String.format("%.1f", maximaCalificacionAlcanzable)}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100) // Naranja oscuro
                                    )
                                    Text(
                                        text = "Necesitarías: ${String.format("%.1f", calificacionNecesaria)} (máximo es 10.0)",
                                        fontSize = 12.sp,
                                        color = AppTheme.colors.TextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Mostrar mensaje de alerta si es una evaluación pendiente
            evaluacionPendiente?.let { pendiente ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            pendiente.tipo == TipoPendiente.FECHA_VENCIDA -> Color(0xFFFFEBEE) // Rojo claro
                            pendiente.tipo == TipoPendiente.HOY -> Color(0xFFFFF3E0) // Naranja claro
                            else -> Color(0xFFE3F2FD) // Azul claro
                        }
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            pendiente.tipo == TipoPendiente.FECHA_VENCIDA -> Color(0xFFD32F2F) // Rojo
                            pendiente.tipo == TipoPendiente.HOY -> Color(0xFFFF9800) // Naranja
                            else -> Color(0xFF2196F3) // Azul
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                pendiente.tipo == TipoPendiente.FECHA_VENCIDA -> "⚠️"
                                pendiente.tipo == TipoPendiente.HOY -> "📅"
                                else -> "📋"
                            },
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = pendiente.mensaje,
                            fontSize = 14.sp,
                            color = AppTheme.colors.TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEvaluacionDialog(
    evaluacion: Evaluacion,
    evaluaciones: List<Evaluacion>,
    currentUser: com.google.firebase.auth.FirebaseUser?,
    semestreId: String,
    materiaId: String,
    dataViewModel: DataViewModel,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedEvaluacion by remember { mutableStateOf(evaluacion) }
    var calificacion by remember { mutableStateOf(if (evaluacion.calificacionResultado > 0.0) evaluacion.calificacionResultado.toString() else "") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Validar fecha
    val fechaEvaluacion = selectedEvaluacion.fecha
    val fechaActual = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    val fechaEvaluacionDate = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaEvaluacion)
    } catch (e: Exception) {
        null
    }
    val fechaActualDate = try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fechaActual)
    } catch (e: Exception) {
        null
    }
    val fechaCumplida = fechaEvaluacionDate != null && fechaActualDate != null &&
        (fechaEvaluacionDate?.before(fechaActualDate) == true || fechaEvaluacionDate?.equals(fechaActualDate) == true)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Agregar Resultado",
                color = AppTheme.colors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Dropdown para seleccionar evaluación
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedEvaluacion.nombre,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Seleccionar Evaluación", color = AppTheme.colors.TextPrimary) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AppTheme.colors.TextPrimary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            focusedLabelColor = AppTheme.colors.ButtonPrimary,
                            focusedTextColor = AppTheme.colors.TextPrimary,
                            unfocusedTextColor = AppTheme.colors.TextPrimary
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        evaluaciones.forEach { eval ->
                            val evalTieneCalificacion = eval.calificacionResultado > 0.0
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${eval.nombre} ${if (evalTieneCalificacion) "(${eval.calificacionResultado})" else "(Pendiente)"}",
                                        color = AppTheme.colors.TextPrimary
                                    )
                                },
                                onClick = {
                                    selectedEvaluacion = eval
                                    calificacion = if (eval.calificacionResultado > 0.0) eval.calificacionResultado.toString() else ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Fecha: ${selectedEvaluacion.fecha}",
                    fontSize = 14.sp,
                    color = if (fechaCumplida) AppTheme.colors.ButtonPrimary else AppTheme.colors.TextTertiary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (!fechaCumplida) {
                    Text(
                        text = "⚠️ No puedes agregar resultado hasta que se cumpla la fecha",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    Text(
                        text = "✅ Fecha cumplida - Puedes agregar resultado",
                        fontSize = 12.sp,
                        color = AppTheme.colors.ButtonPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                OutlinedTextField(
                    value = calificacion,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.matches(Regex("^\\d{0,2}(\\.\\d{0,1})?$"))) {
                            calificacion = input
                            val cal = input.toDoubleOrNull()
                            errorMessage = when {
                                input.isEmpty() -> ""
                                cal == null -> "Formato inválido"
                                cal < 0.0 -> "Mínimo 0.0"
                                cal > 10.0 -> "Máximo 10.0"
                                else -> ""
                            }
                        }
                    },
                    label = { Text("Calificación (0.0 - 10.0)", color = AppTheme.colors.TextPrimary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    enabled = fechaCumplida,
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
                    val cal = calificacion.toDoubleOrNull()
                    if (cal != null && cal >= 0.0 && cal <= 10.0 && fechaCumplida) {
                        onConfirm(selectedEvaluacion.id, cal)
                    }
                },
                enabled = fechaCumplida &&
                        calificacion.isNotBlank() &&
                        errorMessage.isEmpty() &&
                        calificacion.toDoubleOrNull()?.let { it >= 0.0 && it <= 10.0 } == true
            ) {
                Text("Guardar", color = AppTheme.colors.TextPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = AppTheme.colors.TextPrimary)
            }
        },
        containerColor = AppTheme.colors.BackgroundPrimary
    )
}