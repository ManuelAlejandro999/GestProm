package com.example.gestprom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import com.example.gestprom.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gestprom.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMenu(
    onSemestresClick: () -> Unit = {},      // Navegación a pantalla de Semestres
    onCalculadoraClick: () -> Unit = {},    // Calculadora
    onAtrasClick: () -> Unit = {},          // Acción para regresar
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GestProm",
                        color = AppTheme.colors.TextPrimary,
                        fontSize = 24.sp,
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
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Botones principales
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                // Botón Semestres
                Button(
                    onClick = onSemestresClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppTheme.colors.ButtonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Semestres",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White)
                }

                Spacer(modifier = Modifier.height(15.dp))

                // Botón "¿Cuánto me falta?"
                Button(
                    onClick = {
                        onCalculadoraClick()  // <- Este botón llama a la calculadora
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor =  AppTheme.colors.ButtonPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "¿Cuánto me falta?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenMenuPreview() {
    ScreenMenu()
}