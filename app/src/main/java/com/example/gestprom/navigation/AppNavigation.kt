package com.example.gestprom.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestprom.screens.*
import com.example.gestprom.models.Materia

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
                onCalculadoraClick = { navController.navigate("calculadora") }, // Navegación agregada
                onAtrasClick = { navController.popBackStack() }
            )
        }

        // Nueva ruta para la calculadora
        composable("calculadora") {
            ScreenCalculadora(
                onAtrasClick = { navController.popBackStack() }
            )
        }

        composable("semestres") {
            ScreenSemestres(
                onAtrasClick = { navController.popBackStack() },
                onSemestreClick = { nombreSemestre ->
                    navController.navigate("materias/$nombreSemestre")
                }
            )
        }

        composable("materias/{semestreName}") { backStackEntry ->
            val semestreName = backStackEntry.arguments?.getString("semestreName") ?: ""
            ScreenMaterias(
                semestreName = semestreName,
                onBackClick = { navController.popBackStack() },
                onAddMateriaClick = {
                    // Implementar navegación para agregar materia
                    // navController.navigate("agregar_materia")
                },
                onConfigurarEvaluacionesClick = {
                    navController.navigate("configurar_evaluaciones")
                },
                onMateriaClick = { materia ->
                    // Navegar a evaluaciones de la materia
                    navController.navigate("evaluaciones/${materia.nombre}/${materia.calificacion}")
                }
            )
        }

        composable("evaluaciones/{materiaNombre}/{materiaCalificacion}") { backStackEntry ->
            val materiaNombre = backStackEntry.arguments?.getString("materiaNombre") ?: ""
            val materiaCalificacion = backStackEntry.arguments?.getString("materiaCalificacion")?.toDoubleOrNull() ?: 0

            // Recrear el objeto Materia con los datos de navegación
            val materia = Materia(
                nombre = materiaNombre,
                tipoCalificacion = "Numérica",
                calificacion = materiaCalificacion
            )

            ScreenEvaluaciones(
                materia = materia,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("configurar_evaluaciones") {
            ScreenConfig(
                onBackClick = { navController.popBackStack() },
                onDateChange = { id, fecha ->
                    // Manejar cambio de fecha
                }
            )
        }
    }
}