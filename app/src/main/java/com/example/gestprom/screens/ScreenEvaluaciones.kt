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
import com.example.gestprom.models.Materia
import com.example.gestprom.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.*

// Modelo para las evaluaciones
data class Evaluacion(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val fecha: String,
    val calificacion: Double? = null // null = pendiente
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenEvaluaciones(
    materia: Materia,
    onBackClick: () -> Unit = {}
) {
    // Estado para las evaluaciones - inicializadas con las 4 evaluaciones requeridas
    var evaluaciones by remember {
        mutableStateOf(
            listOf(
                Evaluacion("1", "Parcial 1", "15/02/2026", null),
                Evaluacion("2", "Parcial 2", "15/03/2026", null),
                Evaluacion("3", "Parcial 3", "15/04/2026", null),
                Evaluacion("4", "Ordinario", "25/04/2026", null),
            )
        )
    }

    var showEvaluationDialog by remember { mutableStateOf(false) }
    var selectedEvaluacion by remember { mutableStateOf<Evaluacion?>(null) }

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.BackgroundPrimary // Cambiado para que coincida
                )
            )
        },
        containerColor = AppTheme.colors.BackgroundPrimary // Cambiado para que coincida
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Lista de evaluaciones
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(evaluaciones) { evaluacion ->
                    EvaluacionCard(
                        evaluacion = evaluacion,
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
                    val evaluacionPendiente = evaluaciones.firstOrNull { it.calificacion == null }
                    selectedEvaluacion = evaluacionPendiente ?: evaluaciones.first()
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

    // Dialog para editar calificación de evaluación
    if (showEvaluationDialog && selectedEvaluacion != null) {
        EditEvaluacionDialog(
            evaluacion = selectedEvaluacion!!,
            evaluaciones = evaluaciones,
            onDismiss = {
                showEvaluationDialog = false
                selectedEvaluacion = null
            },
            onConfirm = { evaluacionId, calificacion ->
                evaluaciones = evaluaciones.map { eval ->
                    if (eval.id == evaluacionId) {
                        eval.copy(calificacion = calificacion)
                    } else eval
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
    onClick: () -> Unit
) {
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = evaluacion.nombre,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextSecondary
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mostrar fecha solo si no tiene calificación
                if (evaluacion.calificacion == null) {
                    Text(
                        text = evaluacion.fecha,
                        fontSize = 14.sp,
                        color = AppTheme.colors.TextTertiary
                    )
                } else {
                    Text(
                        text = "Calificación registrada",
                        fontSize = 14.sp,
                        color = AppTheme.colors.ButtonPrimary
                    )
                }
            }

            // Mostrar calificación o "pendiente"
            if (evaluacion.calificacion != null) {
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
                            text = "${evaluacion.calificacion}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.TextSecondary
                        )
                    }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditEvaluacionDialog(
    evaluacion: Evaluacion,
    evaluaciones: List<Evaluacion>,
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var selectedEvaluacion by remember { mutableStateOf(evaluacion) }
    var calificacion by remember { mutableStateOf(evaluacion.calificacion?.toString() ?: "") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.colors.CardBackground
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Agregar Resultado",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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
                        label = { Text("Seleccionar Evaluación", color = AppTheme.colors.TextTertiary) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AppTheme.colors.TextTertiary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AppTheme.colors.ButtonPrimary,
                            focusedLabelColor = AppTheme.colors.ButtonPrimary,
                            focusedTextColor = AppTheme.colors.TextSecondary,
                            unfocusedTextColor = AppTheme.colors.TextSecondary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        evaluaciones.forEach { eval ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "${eval.nombre} ${if (eval.calificacion != null) "(${eval.calificacion})" else "(Pendiente)"}"
                                    )
                                },
                                onClick = {
                                    selectedEvaluacion = eval
                                    calificacion = eval.calificacion?.toString() ?: ""
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mostrar fecha de la evaluación seleccionada
                Text(
                    text = "Fecha: ${selectedEvaluacion.fecha}",
                    fontSize = 14.sp,
                    color = AppTheme.colors.TextTertiary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Campo de calificación
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
                    label = { Text("Calificación (0.0 - 10.0)", color = AppTheme.colors.TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppTheme.colors.ButtonPrimary,
                        focusedLabelColor = AppTheme.colors.ButtonPrimary,
                        focusedTextColor = AppTheme.colors.TextSecondary,
                        unfocusedTextColor = AppTheme.colors.TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.TextTertiary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancelar", color = AppTheme.colors.TextPrimary)
                    }

                    Button(
                        onClick = {
                            val cal = calificacion.toDoubleOrNull()
                            if (cal != null && cal >= 0.0 && cal <= 10.0) {
                                onConfirm(selectedEvaluacion.id, cal)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppTheme.colors.ButtonPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        enabled = calificacion.isNotBlank() &&
                                errorMessage.isEmpty() &&
                                calificacion.toDoubleOrNull()?.let { it >= 0.0 && it <= 10.0 } == true
                    ) {
                        Text("Guardar", color = AppTheme.colors.TextPrimary)
                    }
                }
            }
        }
    }
}