package com.example.gestprom.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenCalculadora(
    onAtrasClick: () -> Unit = {}
) {
    var calificacionObjetivo by remember { mutableStateOf("6.0") }
    var parcial1 by remember { mutableStateOf("") }
    var parcial2 by remember { mutableStateOf("") }
    var parcial3 by remember { mutableStateOf("") }
    var resultado by remember { mutableStateOf<String?>(null) }
    var mostrarResultado by remember { mutableStateOf(false) }

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

            when {
                necesarioOrdinario <= 0 -> "¡Ya tienes la calificación objetivo!"
                necesarioOrdinario > 10 -> "Imposible alcanzar el objetivo"
                else -> String.format("%.1f", necesarioOrdinario)
            }
        } catch (e: Exception) {
            "Error en el cálculo"
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
                            resultado?.contains("Imposible") == true -> Color(0xFFE57373).copy(alpha = 0.1f)
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
                            resultado?.contains("Imposible") == true -> "Imposible alcanzar el objetivo con estas calificaciones"
                            resultado?.contains("Error") == true -> "Error en el cálculo. Verifica los datos"
                            else -> "Necesitas al menos $resultado en el Ordinario para pasar con $calificacionObjetivo"
                        }

                        Text(
                            text = mensajeCompleto,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                resultado?.contains("Ya tienes") == true -> Color(0xFF2E7D32)
                                resultado?.contains("Imposible") == true -> Color(0xFFD32F2F)
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