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
                onComenzarClick = { navController.navigate("login") }
            )
        }

        composable("login") {
            ScreenLogin(
                onLoginClick = { matricula, password ->
                    // Aquí puedes agregar la lógica de validación
                    // Por ahora, cualquier matrícula y contraseña válida permite el acceso
                    if (validarCredenciales(matricula, password)) {
                        navController.navigate("menu") {
                            // Limpia todo el stack para que no se pueda volver al login con el botón atrás
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onRegistrarClick = { navController.navigate("registro") },
                onAtrasClick = {
                    // Verificar si hay pantallas en el stack para regresar
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        // Si no hay pantallas anteriores, navegar a inicio
                        navController.navigate("inicio") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("registro") {
            ScreenRegistro(
                onRegistroClick = { matricula, password, nombre, email ->
                    // Lógica de registro
                    if (registrarUsuario(matricula, password, nombre, email)) {
                        // Registro exitoso, ir directamente al menú
                        navController.navigate("menu") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                },
                onAtrasClick = { navController.popBackStack() }
            )
        }

        composable("menu") {
            ScreenMenu(
                onSemestresClick = { navController.navigate("semestres") },
                onCalculadoraClick = { navController.navigate("calculadora") },
                onCerrarSesionClick = {
                    // Cerrar sesión: ir a inicio y limpiar todo el stack
                    navController.navigate("inicio") {
                        popUpTo(0) { inclusive = true }
                    }
                }
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

// Función de validación básica de credenciales
private fun validarCredenciales(matricula: String, password: String): Boolean {
    // Validación básica: matrícula debe tener al menos 6 caracteres y contraseña al menos 4
    // Aquí puedes implementar tu lógica de validación personalizada

    // Ejemplo de credenciales válidas para demo:
    val credencialesValidas = mapOf(
        "2021001" to "admin123",
        "2021002" to "student123",
        "admin" to "admin"
    )

    return credencialesValidas[matricula] == password ||
            (matricula.length >= 6 && password.length >= 4)
}

// Función de registro de usuario
private fun registrarUsuario(matricula: String, password: String, nombre: String, email: String): Boolean {
    // Validaciones básicas
    if (matricula.length < 6 || password.length < 4 || nombre.isBlank() || email.isBlank()) {
        return false
    }

    // Validar formato de email básico
    if (!email.contains("@") || !email.contains(".")) {
        return false
    }

    // Aquí normalmente guardarías en una base de datos
    // Por ahora, simulamos un registro exitoso
    return true
}