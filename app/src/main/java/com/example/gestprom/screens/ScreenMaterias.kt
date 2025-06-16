package com.example.gestprom.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestprom.models.Materia
import com.example.gestprom.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMaterias(
    onBackClick: () -> Unit = {},
    onAddMateriaClick: () -> Unit = {},
    onConfigurarEvaluacionesClick: () -> Unit = {},
    onMateriaClick: (Materia) -> Unit = {}
) {
    // Estado para las materias - empieza vacío
    var materias by remember { mutableStateOf(emptyList<Materia>()) }
class Materia {

}
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Configurar asignaturas",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 20.sp,
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

        if (materias.isEmpty()) {
            // Pantalla vacía (primera imagen)
            EmptyStateContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAddMateriaClick = onAddMateriaClick,
                onConfigurarEvaluacionesClick = onConfigurarEvaluacionesClick
            )
        } else {
            // Pantalla con materias (segunda imagen)
            MateriasContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                materias = materias,
                onAddMateriaClick = onAddMateriaClick,
                onConfigurarEvaluacionesClick = onConfigurarEvaluacionesClick,
                onMateriaClick = onMateriaClick
            )
        }
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
    onMateriaClick: (Materia) -> Unit
) {
    Column(
        modifier = modifier.padding(24.dp)
    ) {
        // Lista de materias
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(materias) { materia ->
                MateriaCard(
                    materia = materia,
                    onClick = { onMateriaClick(materia) }
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
}

@Composable
private fun MateriaCard(
    materia: Materia,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nombre de la materia
            Text(
                text = materia.nombre,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Tipo de calificación
            Text(
                text = materia.tipoCalificacion,
                fontSize = 14.sp,
                color = AppTheme.colors.TextTertiary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Calificación
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Gray.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = materia.calificacion.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AppTheme.colors.TextSecondary
                    )
                }
            }
        }
    }
}