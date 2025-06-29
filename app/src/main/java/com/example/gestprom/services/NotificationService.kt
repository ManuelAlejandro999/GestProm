package com.example.gestprom.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.gestprom.MainActivity
import com.example.gestprom.R
import com.example.gestprom.models.Evaluacion
import com.example.gestprom.models.Materia
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await

class NotificationService(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val TAG = "NotificationService"

    override suspend fun doWork(): Result {
        Log.d(TAG, "=== INICIANDO WORKER DE NOTIFICACIÓN ===")
        
        val evaluacionId = inputData.getString("evaluacion_id") ?: return Result.failure()
        val materiaId = inputData.getString("materia_id") ?: return Result.failure()
        val semestreId = inputData.getString("semestre_id") ?: return Result.failure()

        Log.d(TAG, "Datos recibidos - EvaluacionID: $evaluacionId, MateriaID: $materiaId, SemestreID: $semestreId")

        try {
            // Obtener datos de la materia
            Log.d(TAG, "Obteniendo datos de la materia...")
            val materiaDoc = firestore.collection("semestres")
                .document(semestreId)
                .collection("materias")
                .document(materiaId)
                .get()
                .await()

            val materia = materiaDoc.toObject(Materia::class.java)
            Log.d(TAG, "Materia obtenida: ${materia?.nombre}")
            
            // Obtener datos de la evaluación
            Log.d(TAG, "Obteniendo datos de la evaluación...")
            val evaluacionDoc = firestore.collection("semestres")
                .document(semestreId)
                .collection("materias")
                .document(materiaId)
                .collection("evaluaciones")
                .document(evaluacionId)
                .get()
                .await()

            val evaluacion = evaluacionDoc.toObject(Evaluacion::class.java)
            Log.d(TAG, "Evaluación obtenida: ${evaluacion?.nombre} - Fecha: ${evaluacion?.fecha}")
            
            if (materia != null && evaluacion != null) {
                Log.d(TAG, "Calculando calificación necesaria...")
                val calificacionNecesaria = calcularCalificacionNecesaria(firestore, materia, semestreId, materiaId, evaluacion)
                Log.d(TAG, "Calificación necesaria calculada: $calificacionNecesaria")
                
                Log.d(TAG, "Mostrando notificación...")
                mostrarNotificacion(materia.nombre, evaluacion.nombre, calificacionNecesaria, materia)
                Log.d(TAG, "Notificación mostrada exitosamente")
            } else {
                Log.e(TAG, "Error: Materia o evaluación es null")
            }

            Log.d(TAG, "=== WORKER COMPLETADO EXITOSAMENTE ===")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error en el worker: ${e.message}", e)
            return Result.failure()
        }
    }

    private fun mostrarNotificacion(nombreMateria: String, nombreEvaluacion: String, calificacionNecesaria: Double, materia: Materia) {
        Log.d(TAG, "Iniciando mostrarNotificacion - Materia: $nombreMateria, Evaluación: $nombreEvaluacion")
        
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "evaluaciones_channel",
                "Notificaciones de Evaluaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordar fechas de evaluaciones"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado")
        }

        // Intent para abrir la app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Determinar el mensaje según el contexto y calificación necesaria
        val mensaje = when {
            calificacionNecesaria > 10.0 -> {
                // No se puede alcanzar el objetivo
                "⚠️ ¡Atención! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Ya no es posible alcanzar tu objetivo. Enfócate en sacar la mejor calificación posible."
            }
            calificacionNecesaria == materia.calificacionObjetivo -> {
                // Es la primera evaluación o no hay calificaciones previas
                "🎯 ¡Hoy es ${nombreEvaluacion} de ${nombreMateria}! Tu objetivo es ${materia.calificacionObjetivo}. ¡Mucha suerte!"
            }
            calificacionNecesaria >= 8.0 -> {
                // Desafío alto
                "🎯 ¡Desafío! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Necesitas sacar ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo. ¡Prepárate bien!"
            }
            calificacionNecesaria >= 6.0 -> {
                // Esfuerzo moderado
                "📚 ¡Esfuerzo requerido! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Necesitas sacar ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo."
            }
            calificacionNecesaria >= 4.0 -> {
                // Esfuerzo bajo
                "✅ ¡Vas bien! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Necesitas sacar ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo."
            }
            calificacionNecesaria >= 2.0 -> {
                // Muy fácil
                "🎉 ¡Excelente progreso! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Solo necesitas ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo."
            }
            else -> {
                // Los parciales ya garantizan el objetivo
                "🎊 ¡Felicidades! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Los parciales ya garantizan tu objetivo. ¡Solo mantén el nivel!"
            }
        }

        // Crear notificación
        val notification = NotificationCompat.Builder(applicationContext, "evaluaciones_channel")
            .setSmallIcon(R.drawable.iconoh)
            .setContentTitle("📚 ¡Evaluación Hoy!")
            .setContentText("$nombreMateria - $nombreEvaluacion")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notificación enviada con ID: $notificationId")
    }
}

// Clase para programar notificaciones
class NotificationScheduler(private val context: Context) {
    private val workManager = androidx.work.WorkManager.getInstance(context)
    private val TAG = "NotificationScheduler"

    fun programarNotificacion(evaluacion: Evaluacion, materia: Materia, semestreId: String) {
        Log.d(TAG, "=== PROGRAMANDO NOTIFICACIÓN ===")
        Log.d(TAG, "Evaluación: ${evaluacion.nombre}, Fecha: ${evaluacion.fecha}")
        Log.d(TAG, "Materia: ${materia.nombre}")
        
        val evaluacionDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            .parse(evaluacion.fecha)
        
        if (evaluacionDate != null) {
            val now = Calendar.getInstance()
            val evaluacionCalendar = Calendar.getInstance().apply {
                time = evaluacionDate
                set(Calendar.HOUR_OF_DAY, 8) // Notificar a las 8 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            Log.d(TAG, "Fecha actual: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(now.time)}")
            Log.d(TAG, "Fecha evaluación: ${SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(evaluacionCalendar.time)}")

            // Si la fecha es hoy, enviar notificación inmediatamente
            if (esMismoDia(evaluacionCalendar, now)) {
                Log.d(TAG, "¡La fecha es hoy! Enviando notificación inmediatamente...")
                enviarNotificacionInmediata(evaluacion, materia, semestreId)
                return
            }

            // Si la fecha ya pasó, no programar
            if (evaluacionCalendar.before(now)) {
                Log.d(TAG, "La fecha ya pasó, no se programa notificación")
                return
            }

            val delayMillis = evaluacionCalendar.timeInMillis - now.timeInMillis
            Log.d(TAG, "Programando notificación para el futuro. Delay: ${delayMillis}ms")

            val notificationWork = androidx.work.OneTimeWorkRequestBuilder<NotificationService>()
                .setInputData(androidx.work.Data.Builder()
                    .putString("evaluacion_id", evaluacion.id)
                    .putString("materia_id", materia.id)
                    .putString("semestre_id", semestreId)
                    .build())
                .setInitialDelay(delayMillis, java.util.concurrent.TimeUnit.MILLISECONDS)
                .addTag(evaluacion.id) // Para poder cancelar la notificación específica
                .build()

            workManager.enqueue(notificationWork)
            Log.d(TAG, "Notificación programada exitosamente")
        } else {
            Log.e(TAG, "Error: No se pudo parsear la fecha: ${evaluacion.fecha}")
        }
    }

    private fun esMismoDia(cal1: Calendar, cal2: Calendar): Boolean {
        val mismoDia = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
        Log.d(TAG, "Verificando si es el mismo día: $mismoDia")
        return mismoDia
    }

    private fun enviarNotificacionInmediata(evaluacion: Evaluacion, materia: Materia, semestreId: String) {
        Log.d(TAG, "Enviando notificación inmediata...")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val calificacionNecesaria = calcularCalificacionNecesaria(
                    FirebaseFirestore.getInstance(), materia, semestreId, materia.id, evaluacion
                )
                withContext(Dispatchers.Main) {
                    mostrarNotificacionDirecta(materia.nombre, evaluacion.nombre, calificacionNecesaria, materia)
                    Log.d(TAG, "Notificación inmediata enviada exitosamente")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar notificación inmediata: ", e)
            }
        }
    }

    private fun mostrarNotificacionDirecta(nombreMateria: String, nombreEvaluacion: String, calificacionNecesaria: Double, materia: Materia) {
        Log.d(TAG, "Iniciando mostrarNotificacionDirecta - Materia: $nombreMateria, Evaluación: $nombreEvaluacion")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Crear canal de notificación para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "evaluaciones_channel",
                "Notificaciones de Evaluaciones",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones para recordar fechas de evaluaciones"
            }
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación creado")
        }

        // Intent para abrir la app
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        // Determinar el mensaje según el contexto y calificación necesaria
        val mensaje = when {
            calificacionNecesaria > 10.0 -> {
                // No se puede alcanzar el objetivo
                "⚠️ ¡Atención! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Ya no es posible alcanzar tu objetivo. Enfócate en sacar la mejor calificación posible."
            }
            calificacionNecesaria == materia.calificacionObjetivo -> {
                // Es la primera evaluación o no hay calificaciones previas
                "🎯 ¡Hoy es ${nombreEvaluacion} de ${nombreMateria}! Tu objetivo es ${materia.calificacionObjetivo}. ¡Mucha suerte!"
            }
            calificacionNecesaria >= 8.0 -> {
                // Desafío alto
                "🎯 ¡Desafío! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Necesitas sacar ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo. ¡Prepárate bien!"
            }
            calificacionNecesaria >= 6.0 -> {
                // Esfuerzo moderado
                "📚 ¡Esfuerzo requerido! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Necesitas sacar ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo."
            }
            calificacionNecesaria >= 4.0 -> {
                // Esfuerzo bajo
                "✅ ¡Vas bien! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Necesitas sacar ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo."
            }
            calificacionNecesaria >= 2.0 -> {
                // Muy fácil
                "🎉 ¡Excelente progreso! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Solo necesitas ${String.format("%.1f", calificacionNecesaria)} para alcanzar tu objetivo."
            }
            else -> {
                // Los parciales ya garantizan el objetivo
                "🎊 ¡Felicidades! ${nombreEvaluacion} de ${nombreMateria} - Objetivo: ${materia.calificacionObjetivo}. Los parciales ya garantizan tu objetivo. ¡Solo mantén el nivel!"
            }
        }

        // Crear notificación
        val notification = NotificationCompat.Builder(context, "evaluaciones_channel")
            .setSmallIcon(R.drawable.iconoh)
            .setContentTitle("📚 ¡Evaluación Hoy!")
            .setContentText("$nombreMateria - $nombreEvaluacion")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(mensaje))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
        Log.d(TAG, "Notificación directa enviada con ID: $notificationId")
    }

    fun cancelarNotificacion(evaluacionId: String) {
        Log.d(TAG, "Cancelando notificación para evaluación: $evaluacionId")
        workManager.cancelAllWorkByTag(evaluacionId)
    }
}

// --- FUNCIONES DE ARCHIVO PARA CÁLCULO DE CALIFICACIÓN NECESARIA ---
suspend fun calcularCalificacionNecesaria(
    firestore: FirebaseFirestore,
    materia: Materia,
    semestreId: String,
    materiaId: String,
    evaluacionActual: Evaluacion
): Double {
    try {
        Log.d("NotificationService", "Iniciando cálculo de calificación necesaria...")
        val evaluacionesSnapshot = firestore.collection("semestres")
            .document(semestreId)
            .collection("materias")
            .document(materiaId)
            .collection("evaluaciones")
            .get()
            .await()
        val evaluaciones = evaluacionesSnapshot.toObjects(Evaluacion::class.java)
        Log.d("NotificationService", "Total de evaluaciones encontradas: ${evaluaciones.size}")
        val parciales = evaluaciones.filter { it.nombre.contains("Parcial", ignoreCase = true) }
        val ordinario = evaluaciones.find { it.nombre.contains("Ordinario", ignoreCase = true) }
        val parcialesCompletados = parciales.filter { it.calificacionResultado > 0 }
        val ordinarioCompletado = ordinario?.takeIf { it.calificacionResultado > 0 }
        val esParcial = evaluacionActual.nombre.contains("Parcial", ignoreCase = true)
        val esOrdinario = evaluacionActual.nombre.contains("Ordinario", ignoreCase = true)
        val resultado = when {
            esParcial -> calcularCalificacionParcial(materia, evaluacionActual, parciales, ordinarioCompletado)
            esOrdinario -> calcularCalificacionOrdinario(materia, parcialesCompletados)
            else -> materia.calificacionObjetivo
        }.let { maxOf(0.0, minOf(10.0, it)) }
        Log.d("NotificationService", "Calificación necesaria calculada: $resultado")
        return resultado
    } catch (e: Exception) {
        Log.e("NotificationService", "Error al calcular calificación necesaria: ${e.message}", e)
        return materia.calificacionObjetivo
    }
}

fun calcularCalificacionParcial(
    materia: Materia,
    evaluacionActual: Evaluacion,
    todosLosParciales: List<Evaluacion>,
    ordinarioCompletado: Evaluacion?
): Double {
    Log.d("NotificationService", "Calculando calificación para parcial: ${evaluacionActual.nombre}")
    val parcialesCompletados = todosLosParciales.filter {
        it.calificacionResultado > 0 && it.id != evaluacionActual.id
    }
    Log.d("NotificationService", "Parciales completados (excluyendo actual): ${parcialesCompletados.size}")
    parcialesCompletados.forEach {
        Log.d("NotificationService", "  - ${it.nombre}: ${it.calificacionResultado}")
    }
    return if (ordinarioCompletado != null) {
        val promedioParcialesActual = if (parcialesCompletados.isNotEmpty()) {
            parcialesCompletados.map { it.calificacionResultado }.average()
        } else 0.0
        Log.d("NotificationService", "Promedio parciales actual: $promedioParcialesActual")
        Log.d("NotificationService", "Ordinario completado: ${ordinarioCompletado.calificacionResultado}")
        val promedioNecesario = (materia.calificacionObjetivo - (ordinarioCompletado.calificacionResultado * 0.5)) / 0.5
        Log.d("NotificationService", "Promedio necesario de parciales: $promedioNecesario")
        val parcialesRestantes = 3 - parcialesCompletados.size
        Log.d("NotificationService", "Parciales restantes: $parcialesRestantes")
        if (parcialesRestantes > 0) {
            val sumaNecesaria = promedioNecesario * 3
            val sumaActual = parcialesCompletados.sumOf { it.calificacionResultado }
            val calificacionNecesaria = (sumaNecesaria - sumaActual) / parcialesRestantes
            Log.d("NotificationService", "Suma necesaria: $sumaNecesaria, Suma actual: $sumaActual")
            Log.d("NotificationService", "Calificación necesaria para ${evaluacionActual.nombre}: $calificacionNecesaria")
            if (calificacionNecesaria > 10.0) {
                val sumaMaxima = (promedioParcialesActual * parcialesCompletados.size) + (10.0 * parcialesRestantes)
                val promedioMaximo = sumaMaxima / 3
                val calificacionMaxima = (promedioMaximo * 0.5) + (ordinarioCompletado.calificacionResultado * 0.5)
                if (calificacionMaxima < materia.calificacionObjetivo) {
                    val promedioParaMaxima = (calificacionMaxima - (ordinarioCompletado.calificacionResultado * 0.5)) / 0.5
                    val sumaParaMaxima = promedioParaMaxima * 3
                    val calificacionParaMaxima = (sumaParaMaxima - sumaActual) / parcialesRestantes
                    maxOf(0.0, minOf(10.0, calificacionParaMaxima))
                } else {
                    maxOf(0.0, minOf(10.0, calificacionNecesaria))
                }
            } else {
                maxOf(0.0, minOf(10.0, calificacionNecesaria))
            }
        } else {
            Log.d("NotificationService", "No hay parciales restantes, usando objetivo")
            materia.calificacionObjetivo
        }
    } else {
        val promedioParcialesActual = if (parcialesCompletados.isNotEmpty()) {
            parcialesCompletados.map { it.calificacionResultado }.average()
        } else 0.0
        Log.d("NotificationService", "Promedio parciales actual (sin ordinario): $promedioParcialesActual")
        val promedioNecesario = materia.calificacionObjetivo
        Log.d("NotificationService", "Promedio necesario de parciales (objetivo completo): $promedioNecesario")
        val parcialesRestantes = 3 - parcialesCompletados.size
        Log.d("NotificationService", "Parciales restantes: $parcialesRestantes")
        if (parcialesRestantes > 0) {
            val sumaNecesaria = promedioNecesario * 3
            val sumaActual = parcialesCompletados.sumOf { it.calificacionResultado }
            val calificacionNecesaria = (sumaNecesaria - sumaActual) / parcialesRestantes
            Log.d("NotificationService", "Suma necesaria: $sumaNecesaria, Suma actual: $sumaActual")
            Log.d("NotificationService", "Calificación necesaria para ${evaluacionActual.nombre}: $calificacionNecesaria")
            if (calificacionNecesaria > 10.0) {
                val sumaMaxima = (promedioParcialesActual * parcialesCompletados.size) + (10.0 * parcialesRestantes)
                val promedioMaximo = sumaMaxima / 3
                val calificacionMaxima = promedioMaximo * 0.5 + 5.0
                val promedioParaMaxima = calificacionMaxima * 2
                val sumaParaMaxima = promedioParaMaxima * 3
                val calificacionParaMaxima = (sumaParaMaxima - sumaActual) / parcialesRestantes
                maxOf(0.0, minOf(10.0, calificacionParaMaxima))
            } else {
                maxOf(0.0, minOf(10.0, calificacionNecesaria))
            }
        } else {
            Log.d("NotificationService", "No hay parciales restantes, usando objetivo")
            materia.calificacionObjetivo
        }
    }
}

fun calcularCalificacionOrdinario(
    materia: Materia,
    parcialesCompletados: List<Evaluacion>
): Double {
    Log.d("NotificationService", "Calculando calificación para ordinario")
    Log.d("NotificationService", "Parciales completados: ${parcialesCompletados.size}")
    if (parcialesCompletados.isEmpty()) {
        Log.d("NotificationService", "No hay parciales completados, ordinario necesita el objetivo: ${materia.calificacionObjetivo}")
        return materia.calificacionObjetivo
    }
    val promedioParciales = parcialesCompletados.map { it.calificacionResultado }.average()
    Log.d("NotificationService", "Promedio de parciales: $promedioParciales")
    val calificacionNecesaria = (materia.calificacionObjetivo - (promedioParciales * 0.5)) / 0.5
    Log.d("NotificationService", "Calificación necesaria en ordinario: $calificacionNecesaria")
    if (calificacionNecesaria > 10.0) {
        val calificacionMaxima = (promedioParciales * 0.5) + (10.0 * 0.5)
        Log.d("NotificationService", "No se puede alcanzar el objetivo. Máxima calificación posible: $calificacionMaxima")
        return 10.0
    }
    if (calificacionNecesaria < 0) {
        Log.d("NotificationService", "Los parciales ya garantizan el objetivo. Calificación mínima necesaria en ordinario: 0.0")
        return 0.0
    }
    return maxOf(0.0, minOf(10.0, calificacionNecesaria))
}

// --- FIN FUNCIONES DE ARCHIVO --- 