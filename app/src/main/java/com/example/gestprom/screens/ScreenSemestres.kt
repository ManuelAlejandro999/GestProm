package com.example.gestprom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import com.example.gestprom.R
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gestprom.ui.theme.AppTheme


@Composable
fun ScreenSemestres(
    onAtrasClick: () -> Unit = {},
    onSemestreClick: (String) -> Unit = {}
) {
    var semestres by remember { mutableStateOf(mutableListOf<String>()) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf(-1) }
    var tempNombre by remember { mutableStateOf("") }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
    var indexToDelete by remember { mutableStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundPrimary)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Header con botón atrás
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.atras),
                contentDescription = "Atrás",
                modifier = Modifier
                    .size(40.dp)
                    .clickable { onAtrasClick() }
                    .align(Alignment.CenterStart)
            )
        }

        Spacer(modifier = Modifier.height(15.dp))

        // Título
        Text(
            text = "Semestres",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

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
            colors = ButtonDefaults.buttonColors(containerColor =  AppTheme.colors.ButtonPrimary),
            shape = RoundedCornerShape(12.dp),
            enabled = semestres.size < 12
        ) {
            Text(
                text = if (semestres.size < 12) "+  Añadir Semestre" else "Máximo 12 semestres",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD9D9D9)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = semestre,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray
                        )
                    }

                    // Botón editar
                    IconButton(
                        onClick = {
                            editingIndex = index
                            tempNombre = semestre
                            showEditDialog = true
                        },
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFF606060), RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.configurar),
                            contentDescription = "Editar",
                            modifier = Modifier.size(35.dp)
                        )
                    }

                    // Botón eliminar
//                    IconButton(
//                        onClick = {
//                            indexToDelete = index
//                            showConfirmDeleteDialog = true
//                        },
//
//                        modifier = Modifier
//                            .size(56.dp)
//                            .background(Color(0xFF7C2121), RoundedCornerShape(12.dp))
//                    ) {
//                        Image(
//                            painter = painterResource(id = R.drawable.borrar),
//                            contentDescription = "Eliminar",
//                            modifier = Modifier.size(35.dp)
//                        )
//                    }
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
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Editar Nombre",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF01102C),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = tempNombre,
                        onValueChange = { tempNombre = it },
                        label = { Text("Nombre del semestre") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0945D2),
                            focusedLabelColor = Color(0xFF0945D2)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Cancelar", color = Color.White)
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
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0945D2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Guardar", color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Dialog de confirmación de eliminación
    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = {
                Text(text = "Confirmar eliminación", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¿Estás seguro de que deseas eliminar este semestre?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        semestres = semestres.toMutableList().apply {
                            removeAt(indexToDelete)
                        }
                        showConfirmDeleteDialog = false
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showConfirmDeleteDialog = false }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenSemestresPreview() {
    ScreenSemestres()
}
