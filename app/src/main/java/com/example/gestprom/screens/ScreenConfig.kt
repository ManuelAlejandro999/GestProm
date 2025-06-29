package com.example.gestprom.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gestprom.models.Evaluacion
import com.example.gestprom.models.Semestre
import com.example.gestprom.ui.theme.AppTheme
import com.example.gestprom.viewmodels.AuthViewModel
import com.example.gestprom.viewmodels.DataState
import com.example.gestprom.viewmodels.DataViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenConfig(
    semestreId: String = "",
    authViewModel: AuthViewModel = viewModel(),
    dataViewModel: DataViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onDateChange: (String, String) -> Unit = { _, _ -> }
) {
    // Estado para las evaluaciones
    var evaluaciones by remember {
        mutableStateOf(
            listOf(
                Evaluacion(id = "1", nombre = "Parcial 1", fecha = "15/02/2026"),
                Evaluacion(id = "2", nombre = "Parcial 2", fecha = "15/03/2026"),
                Evaluacion(id = "3", nombre = "Parcial 3", fecha = "15/04/2026"),
                Evaluacion(id = "4", nombre = "Ordinario", fecha = "25/04/2026")
            )
        )
    }

    val currentUser by authViewModel.currentUser.collectAsState()
    val semestres by dataViewModel.semestres.collectAsState()
    val semestresState by dataViewModel.semestresState.collectAsState()

    // Load semestres when user is available
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            dataViewModel.loadSemestres(user.uid)
        }
    }

    // Update evaluaciones when semestres are loaded
    LaunchedEffect(semestres) {
        if (semestreId.isNotEmpty()) {
            val semestre = semestres.find { it.id == semestreId }
            semestre?.let { s ->
                evaluaciones = evaluaciones.map { eval ->
                    when (eval.nombre) {
                        "Parcial 1" -> eval.copy(fecha = s.configuracionEvaluaciones.parcial1_fecha.ifEmpty { "15/02/2026" })
                        "Parcial 2" -> eval.copy(fecha = s.configuracionEvaluaciones.parcial2_fecha.ifEmpty { "15/03/2026" })
                        "Parcial 3" -> eval.copy(fecha = s.configuracionEvaluaciones.parcial3_fecha.ifEmpty { "15/04/2026" })
                        "Ordinario" -> eval.copy(fecha = s.configuracionEvaluaciones.ordinario_fecha.ifEmpty { "25/04/2026" })
                        else -> eval
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurar evaluaciones",
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
        if (semestresState is DataState.Loading) {
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
        } else if (semestresState is DataState.Error) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (semestresState as DataState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(evaluaciones) { evaluacion: Evaluacion ->
                    EvaluacionCard(
                        evaluacion = evaluacion,
                        onDateClick = { nuevaFecha ->
                            evaluaciones = evaluaciones.map { eval ->
                                if (eval.id == evaluacion.id) {
                                    eval.copy(fecha = nuevaFecha)
                                } else eval
                            }
                            onDateChange(evaluacion.id, nuevaFecha)
                            
                            // Guardar en Firebase si tenemos usuario y semestre
                            if (currentUser != null && semestreId.isNotEmpty()) {
                                val semestreActual = semestres.find { it.id == semestreId }
                                semestreActual?.let { semestre ->
                                    val configuracionActualizada = semestre.configuracionEvaluaciones.copy(
                                        parcial1_fecha = if (evaluacion.nombre == "Parcial 1") nuevaFecha else semestre.configuracionEvaluaciones.parcial1_fecha,
                                        parcial2_fecha = if (evaluacion.nombre == "Parcial 2") nuevaFecha else semestre.configuracionEvaluaciones.parcial2_fecha,
                                        parcial3_fecha = if (evaluacion.nombre == "Parcial 3") nuevaFecha else semestre.configuracionEvaluaciones.parcial3_fecha,
                                        ordinario_fecha = if (evaluacion.nombre == "Ordinario") nuevaFecha else semestre.configuracionEvaluaciones.ordinario_fecha
                                    )
                                    val semestreActualizado = semestre.copy(
                                        configuracionEvaluaciones = configuracionActualizada
                                    )
                                    
                                    dataViewModel.updateSemestre(currentUser!!.uid, semestreActualizado)

                                    // ACTUALIZAR TODAS LAS EVALUACIONES DE TODAS LAS MATERIAS DEL SEMESTRE
                                    dataViewModel.updateFechasEvaluacionesDeSemestre(
                                        userId = currentUser!!.uid,
                                        semestreId = semestreId,
                                        config = configuracionActualizada
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EvaluacionCard(
    evaluacion: Evaluacion,
    onDateClick: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.colors.CardBackground
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nombre de la evaluación
            Text(
                text = evaluacion.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Label "Fecha"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Fecha",
                    fontSize = 14.sp,
                    color = AppTheme.colors.ButtonPrimary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Campo de fecha clickeable
            Card(
                onClick = { showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.colors.TextTertiary.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Seleccionar fecha",
                            tint = AppTheme.colors.TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = evaluacion.fecha,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AppTheme.colors.TextSecondary
                        )
                    }
                }
            }
        }
    }

    // DatePicker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            currentDate = evaluacion.fecha,
            onDateSelected = { nuevaFecha ->
                onDateClick(nuevaFecha)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    currentDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Inicializar el DatePickerState con la fecha actual si existe
    val initialDateMillis = try {
        if (currentDate.isNotEmpty()) {
            val parts = currentDate.split("/")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val month = parts[1].toInt() - 1 // Calendar.MONTH es 0-based
                val year = parts[2].toInt()
                val calendar = Calendar.getInstance()
                calendar.set(year, month, day, 12, 0, 0) // Usar 12:00 para evitar problemas de zona horaria
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.timeInMillis
            } else null
        } else null
    } catch (e: Exception) {
        null
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis ?: System.currentTimeMillis()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convertir millis a formato dd/MM/yyyy de manera más directa
                        val calendar = Calendar.getInstance()
                        calendar.timeInMillis = millis
                        
                        // Usar UTC para evitar problemas de zona horaria
                        val utcCalendar = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        utcCalendar.timeInMillis = millis
                        
                        val day = utcCalendar.get(Calendar.DAY_OF_MONTH)
                        val month = utcCalendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-based
                        val year = utcCalendar.get(Calendar.YEAR)
                        
                        val fechaFormateada = String.format("%02d/%02d/%04d", day, month, year)
                        onDateSelected(fechaFormateada)
                    }
                }
            ) {
                Text(
                    text = "Confirmar",
                    color = AppTheme.colors.ButtonPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    color = AppTheme.colors.TextTertiary
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = AppTheme.colors.TextSecondary
            )
        )
    }
}