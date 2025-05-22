package com.example.gestprom.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestprom.screens.ScreenInicio
import com.example.gestprom.screens.ScreenMenu
import com.example.gestprom.screens.ScreenSemestres

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = "inicio") {

        composable("inicio") {
            ScreenInicio(
                onComenzarClick = { navController.navigate("menu") }
            )
        }

        composable("menu") {
            ScreenMenu(
                onSemestresClick = { navController.navigate("semestres") },
                onAtrasClick = { navController.popBackStack() }
            )
        }

        composable("semestres") {
            ScreenSemestres(
                onAtrasClick = { navController.popBackStack() },
                onSemestreClick = { nombreSemestre ->
                    // Aquí puedes navegar a otra pantalla futura con parámetros
                    // navController.navigate("detalles/$nombreSemestre")
                }
            )
        }
    }
}
