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
import com.example.gestprom.models.Evaluacion
import com.example.gestprom.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenConfig(
    onBackClick: () -> Unit = {},
    onDateChange: (Int, String) -> Unit = { _, _ -> }
) {
    // Estado para las evaluaciones
    var evaluaciones by remember {
        mutableStateOf(
            listOf(
                Evaluacion(1, "Parcial 1", "15/02/2026"),
                Evaluacion(2, "Parcial 2", "15/03/2026"),
                Evaluacion(3, "Parcial 3", "15/04/2026"),
                Evaluacion(4, "Ordinario", "25/04/2026")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurar evaluaciones",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 24.sp, // Tamaño estandarizado
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(evaluaciones) { evaluacion ->
                EvaluacionCard(
                    evaluacion = evaluacion,
                    onDateClick = { nuevaFecha ->
                        evaluaciones = evaluaciones.map { eval ->
                            if (eval.id == evaluacion.id) {
                                eval.copy(fecha = nuevaFecha)
                            } else eval
                        }
                        onDateChange(evaluacion.id, nuevaFecha)
                    }
                )
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
            onDateSelected = { fecha ->
                onDateClick(fecha)
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
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Convertir millis a formato dd/MM/yyyy
                        val date = Date(millis)
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        onDateSelected(formatter.format(date))
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