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
import com.example.gestprom.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenSemestres(
    onAtrasClick: () -> Unit = {},
    onSemestreClick: (String) -> Unit = {}
) {
    var semestres by remember { mutableStateOf(mutableListOf<String>()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf(-1) }
    var tempNombre by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Semestres",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 24.sp, // Tamaño estandarizado
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
                onClick = {
                    if (semestres.size < 12) {
                        semestres = semestres.toMutableList().apply {
                            add("Semestre ${size + 1}")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.colors.ButtonPrimary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = semestres.size < 12
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AppTheme.colors.TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (semestres.size < 12) "Añadir Semestre" else "Máximo 12 semestres",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = AppTheme.colors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

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
                            onClick = { onSemestreClick(semestre) },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.CardBackground
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = semestre,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = AppTheme.colors.TextSecondary
                            )
                        }

                        // Botón editar con icono de menú vertical
                        IconButton(
                            onClick = {
                                editingIndex = index
                                tempNombre = semestre
                                showEditDialog = true
                            },
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
                    }
                }
            }
        }
    }

    // Dialog para editar nombre
    if (showEditDialog) {
        Dialog(onDismissRequest = { showEditDialog = false }) {
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
                        text = "Editar Nombre",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.TextSecondary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = tempNombre,
                        onValueChange = { tempNombre = it },
                        label = {
                            Text(
                                "Nombre del semestre",
                                color = AppTheme.colors.TextTertiary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
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
                            onClick = { showEditDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.TextTertiary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancelar",
                                color = AppTheme.colors.TextPrimary
                            )
                        }

                        Button(
                            onClick = {
                                if (tempNombre.isNotBlank()) {
                                    semestres = semestres.toMutableList().apply {
                                        set(editingIndex, tempNombre)
                                    }
                                }
                                showEditDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AppTheme.colors.ButtonPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Guardar",
                                color = AppTheme.colors.TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}