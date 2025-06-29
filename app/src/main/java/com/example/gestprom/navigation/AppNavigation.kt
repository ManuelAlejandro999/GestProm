package com.example.gestprom.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.gestprom.screens.*
import com.example.gestprom.models.Materia
import com.example.gestprom.viewmodels.AuthState
import com.example.gestprom.viewmodels.AuthViewModel
import com.example.gestprom.viewmodels.DataViewModel

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = "inicio") {

        composable("inicio") {
            ScreenInicio(
                onComenzarClick = { navController.navigate("login") }
            )
        }

        composable("login") {
            ScreenLogin(
                authViewModel = authViewModel,
                onRegistrarClick = { navController.navigate("registro") },
                onAtrasClick = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                onLoginSuccess = {
                    navController.navigate("menu") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("registro") {
            ScreenRegistro(
                authViewModel = authViewModel,
                onAtrasClick = { navController.popBackStack() },
                onRegistroSuccess = {
                    navController.navigate("menu") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("menu") {
            ScreenMenu(
                onSemestresClick = { navController.navigate("semestres") },
                onCalculadoraClick = { navController.navigate("calculadora") },
                onCerrarSesionClick = {
                    authViewModel.logout()
                    navController.navigate("inicio") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable("calculadora") {
            ScreenCalculadora(
                onAtrasClick = { navController.popBackStack() }
            )
        }

        composable("semestres") {
            ScreenSemestres(
                onAtrasClick = { navController.popBackStack() },
                onSemestreClick = { semestreId ->
                    navController.navigate("materias/$semestreId")
                }
            )
        }

        composable("materias/{semestreId}") { backStackEntry ->
            val semestreId = backStackEntry.arguments?.getString("semestreId") ?: ""
            val dataViewModel: DataViewModel = viewModel()
            val semestres by dataViewModel.semestres.collectAsState()
            val semestre = semestres.find { it.id == semestreId }
            val semestreName = semestre?.nombre ?: ""
            ScreenMaterias(
                semestreName = semestreName,
                semestreId = semestreId,
                onBackClick = { navController.popBackStack() },
                onAddMateriaClick = {
                    // Implementar navegación para agregar materia
                    // navController.navigate("agregar_materia")
                },
                onConfigurarEvaluacionesClick = {
                    navController.navigate("configurar_evaluaciones/$semestreId")
                },
                onMateriaClick = { materia ->
                    // Navegar a evaluaciones de la materia
                    navController.navigate("evaluaciones/${materia.id}/$semestreId")
                }
            )
        }

        composable("evaluaciones/{materiaId}/{semestreId}") { backStackEntry ->
            val materiaId = backStackEntry.arguments?.getString("materiaId") ?: ""
            val semestreId = backStackEntry.arguments?.getString("semestreId") ?: ""

            // Buscar la materia en el DataViewModel
            val dataViewModel: DataViewModel = viewModel()
            val materias by dataViewModel.materias.collectAsState()
            val materia = materias.find { it.id == materiaId } ?: Materia(
                id = materiaId,
                nombre = "Materia",
                calificacionObjetivo = 0.0
            )

            ScreenEvaluaciones(
                materia = materia,
                semestreId = semestreId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable("configurar_evaluaciones/{semestreId}") { backStackEntry ->
            val semestreId = backStackEntry.arguments?.getString("semestreId") ?: ""
            ScreenConfig(
                semestreId = semestreId,
                onBackClick = { navController.popBackStack() },
                onDateChange = { id, fecha ->
                    // Manejar cambio de fecha
                }
            )
        }
    }
}