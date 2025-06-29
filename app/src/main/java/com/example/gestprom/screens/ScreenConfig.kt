package com.example.gestprom.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenConfig(
    semestreId: String = "",
    authViewModel: AuthViewModel = viewModel(),
    dataViewModel: DataViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onDateChange: (String, String) -> Unit = { _, _ -> }
) {
    // Estado para las evaluaciones
    var evaluaciones by remember {
        mutableStateOf(
            listOf(
                Evaluacion(id = "1", nombre = "Parcial 1", fecha = ""),
                Evaluacion(id = "2", nombre = "Parcial 2", fecha = ""),
                Evaluacion(id = "3", nombre = "Parcial 3", fecha = ""),
                Evaluacion(id = "4", nombre = "Ordinario", fecha = "")
            )
        )
    }

    // Estado para mostrar feedback de actualización
    var isUpdatingDates by remember { mutableStateOf(false) }
    var lastUpdatedEvaluation by remember { mutableStateOf<String?>(null) }

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
                // Obtener fecha actual
                val calendar = Calendar.getInstance()
                val currentYear = calendar.get(Calendar.YEAR)
                val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-based
                val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                
                // Fechas por defecto basadas en la fecha actual
                val defaultParcial1 = String.format("%02d/%02d/%04d", currentDay, currentMonth, currentYear)
                val defaultParcial2 = String.format("%02d/%02d/%04d", currentDay, (currentMonth + 1).takeIf { it <= 12 } ?: 1, currentYear)
                val defaultParcial3 = String.format("%02d/%02d/%04d", currentDay, (currentMonth + 2).takeIf { it <= 12 } ?: 1, currentYear)
                val defaultOrdinario = String.format("%02d/%02d/%04d", currentDay, (currentMonth + 3).takeIf { it <= 12 } ?: 1, currentYear)
                
                evaluaciones = evaluaciones.map { eval ->
                    when (eval.nombre) {
                        "Parcial 1" -> eval.copy(fecha = s.configuracionEvaluaciones.parcial1_fecha.ifEmpty { defaultParcial1 })
                        "Parcial 2" -> eval.copy(fecha = s.configuracionEvaluaciones.parcial2_fecha.ifEmpty { defaultParcial2 })
                        "Parcial 3" -> eval.copy(fecha = s.configuracionEvaluaciones.parcial3_fecha.ifEmpty { defaultParcial3 })
                        "Ordinario" -> eval.copy(fecha = s.configuracionEvaluaciones.ordinario_fecha.ifEmpty { defaultOrdinario })
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
                        text = "Configurar Evaluaciones",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
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
                // Mostrar mensaje de actualización si está en curso
                if (isUpdatingDates) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = AppTheme.colors.ButtonPrimary.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = AppTheme.colors.ButtonPrimary,
                                    strokeWidth = 2.dp
                                )
                                Text(
                                    text = "Actualizando fechas en todas las materias...",
                                    color = AppTheme.colors.ButtonPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Mostrar mensaje de éxito si se actualizó recientemente
                if (lastUpdatedEvaluation != null && !isUpdatingDates) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Fechas actualizadas en todas las materias",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                items(evaluaciones) { evaluacion: Evaluacion ->
                    EvaluacionCard(
                        evaluacion = evaluacion,
                        isUpdating = isUpdatingDates && lastUpdatedEvaluation == evaluacion.nombre,
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
                                    
                                    // Mostrar estado de actualización
                                    isUpdatingDates = true
                                    lastUpdatedEvaluation = evaluacion.nombre
                                    
                                    dataViewModel.updateSemestre(currentUser!!.uid, semestreActualizado)

                                    // ACTUALIZAR TODAS LAS EVALUACIONES DE TODAS LAS MATERIAS DEL SEMESTRE
                                    dataViewModel.updateFechasEvaluacionesDeSemestre(
                                        userId = currentUser!!.uid,
                                        semestreId = semestreId,
                                        config = configuracionActualizada
                                    )
                                    
                                    // Ocultar estado de actualización después de un tiempo
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(3000) // 3 segundos
                                        isUpdatingDates = false
                                        // Mantener el mensaje de éxito por un poco más de tiempo
                                        delay(2000) // 2 segundos más
                                        lastUpdatedEvaluation = null
                                    }
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
    isUpdating: Boolean,
    onDateClick: (String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isUpdating) {
                AppTheme.colors.ButtonPrimary.copy(alpha = 0.05f)
            } else {
                AppTheme.colors.CardBackground
            }
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Nombre de la evaluación con indicador de actualización
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = evaluacion.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                
                // Indicador de actualización
                if (isUpdating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = AppTheme.colors.ButtonPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Actualizando...",
                            fontSize = 12.sp,
                            color = AppTheme.colors.ButtonPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

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
                onClick = { if (!isUpdating) showDatePicker = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isUpdating) {
                        AppTheme.colors.TextTertiary.copy(alpha = 0.2f)
                    } else {
                        AppTheme.colors.TextTertiary.copy(alpha = 0.3f)
                    }
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
                            tint = if (isUpdating) {
                                AppTheme.colors.TextTertiary
                            } else {
                                AppTheme.colors.TextSecondary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = evaluacion.fecha,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isUpdating) {
                                AppTheme.colors.TextTertiary
                            } else {
                                AppTheme.colors.TextSecondary
                            }
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
            } else {
                // Si no hay fecha válida, usar la fecha actual
                System.currentTimeMillis()
            }
        } else {
            // Si no hay fecha, usar la fecha actual
            System.currentTimeMillis()
        }
    } catch (e: Exception) {
        // En caso de error, usar la fecha actual
        System.currentTimeMillis()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
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