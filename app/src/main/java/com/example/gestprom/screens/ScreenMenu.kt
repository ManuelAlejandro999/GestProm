package com.example.gestprom.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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


@Composable
fun ScreenMenu(
    onSemestresClick: () -> Unit = {},      // Navegación a pantalla de Semestres
    onCalculadoraClick: () -> Unit = {},  // Calculadora
    onAtrasClick: () -> Unit = {},          // Acción para regresar
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.BackgroundPrimary)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Botón de "Atrás"
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.flecha),
                contentDescription = "Atrás",
                modifier = Modifier
                    .size(25.dp)
                    .clickable { onAtrasClick() }
            )
        }

        // Título centrado
        Column(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text(
                text = "GestProm",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

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

                     onCalculadoraClick()
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

@Preview(showBackground = true)
@Composable
fun ScreenMenuPreview() {
    ScreenMenu()
}
