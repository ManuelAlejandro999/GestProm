package com.example.gestprom.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestprom.models.*
import com.example.gestprom.services.FirestoreService
import com.example.gestprom.services.NotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class DataViewModel : ViewModel() {
    private val firestoreService = FirestoreService()
    private var notificationScheduler: NotificationScheduler? = null

    init {
        println("DEBUG: DataViewModel - Constructor ejecutado")
    }

    // Semestres
    private val _semestres = MutableStateFlow<List<Semestre>>(emptyList())
    val semestres: StateFlow<List<Semestre>> = _semestres.asStateFlow()

    private val _semestresState = MutableStateFlow<DataState>(DataState.Initial)
    val semestresState: StateFlow<DataState> = _semestresState.asStateFlow()

    // Materias
    private val _materias = MutableStateFlow<List<Materia>>(emptyList())
    val materias: StateFlow<List<Materia>> = _materias.asStateFlow()

    private val _materiasState = MutableStateFlow<DataState>(DataState.Initial)
    val materiasState: StateFlow<DataState> = _materiasState.asStateFlow()

    // Evaluaciones
    private val _evaluaciones = MutableStateFlow<List<Evaluacion>>(emptyList())
    val evaluaciones: StateFlow<List<Evaluacion>> = _evaluaciones.asStateFlow()

    private val _evaluacionesState = MutableStateFlow<DataState>(DataState.Initial)
    val evaluacionesState: StateFlow<DataState> = _evaluacionesState.asStateFlow()

    // Current selections
    private val _currentSemestreId = MutableStateFlow<String?>(null)
    val currentSemestreId: StateFlow<String?> = _currentSemestreId.asStateFlow()

    private val _currentMateriaId = MutableStateFlow<String?>(null)
    val currentMateriaId: StateFlow<String?> = _currentMateriaId.asStateFlow()

    // Inicializar el NotificationScheduler
    fun initializeNotifications(context: Context) {
        println("DEBUG: DataViewModel - Inicializando NotificationScheduler...")
        println("DEBUG: DataViewModel - Context recibido: ${context != null}")
        try {
            notificationScheduler = NotificationScheduler(context)
            println("DEBUG: DataViewModel - NotificationScheduler creado: ${notificationScheduler != null}")
            println("DEBUG: DataViewModel - NotificationScheduler inicializado: ${notificationScheduler != null}")
        } catch (e: Exception) {
            println("DEBUG: DataViewModel - ERROR al crear NotificationScheduler: ${e.message}")
            e.printStackTrace()
        }
    }

    // Semestre operations
    fun loadSemestres(userId: String) {
        viewModelScope.launch {
            _semestresState.value = DataState.Loading
            try {
                val result = firestoreService.getSemestres(userId)
                result.fold(
                    onSuccess = { semestres ->
                        println("[DEBUG] ViewModel: Semestres obtenidos: ${semestres.size}")
                        semestres.forEach { println("[DEBUG] ViewModel: Semestre - ID: ${it.id}, Nombre: ${it.nombre}") }
                        _semestres.value = semestres
                        _semestresState.value = DataState.Success
                    },
                    onFailure = { exception ->
                        _semestresState.value = DataState.Error(exception.message ?: "Error al cargar semestres")
                    }
                )
            } catch (e: Exception) {
                _semestresState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createSemestre(userId: String, semestre: Semestre) {
        viewModelScope.launch {
            _semestresState.value = DataState.Loading
            try {
                val result = firestoreService.createSemestre(userId, semestre)
                result.fold(
                    onSuccess = { semestreId ->
                        loadSemestres(userId) // Reload semestres
                    },
                    onFailure = { exception ->
                        _semestresState.value = DataState.Error(exception.message ?: "Error al crear semestre")
                    }
                )
            } catch (e: Exception) {
                _semestresState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun updateSemestre(userId: String, semestre: Semestre) {
        viewModelScope.launch {
            _semestresState.value = DataState.Loading
            try {
                val result = firestoreService.updateSemestre(userId, semestre)
                result.fold(
                    onSuccess = {
                        loadSemestres(userId) // Reload semestres
                    },
                    onFailure = { exception ->
                        _semestresState.value = DataState.Error(exception.message ?: "Error al actualizar semestre")
                    }
                )
            } catch (e: Exception) {
                _semestresState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun deleteSemestre(userId: String, semestreId: String) {
        viewModelScope.launch {
            _semestresState.value = DataState.Loading
            try {
                val result = firestoreService.deleteSemestre(userId, semestreId)
                result.fold(
                    onSuccess = {
                        loadSemestres(userId) // Reload semestres
                    },
                    onFailure = { exception ->
                        _semestresState.value = DataState.Error(exception.message ?: "Error al eliminar semestre")
                    }
                )
            } catch (e: Exception) {
                _semestresState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    // Materia operations
    fun loadMaterias(userId: String, semestreId: String) {
        viewModelScope.launch {
            _materiasState.value = DataState.Loading
            try {
                println("DEBUG: Cargando materias para userId: $userId, semestreId: $semestreId")
                val result = firestoreService.getMaterias(userId, semestreId)
                result.fold(
                    onSuccess = { materias ->
                        println("DEBUG: Materias cargadas exitosamente: ${materias.size} materias")
                        materias.forEach { materia ->
                            println("DEBUG: Materia - ID: ${materia.id}, Nombre: ${materia.nombre}")
                        }
                        _materias.value = materias
                        _materiasState.value = DataState.Success
                    },
                    onFailure = { exception ->
                        println("DEBUG: Error al cargar materias: ${exception.message}")
                        _materiasState.value = DataState.Error(exception.message ?: "Error al cargar materias")
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Excepción al cargar materias: ${e.message}")
                _materiasState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createMateria(userId: String, semestreId: String, materia: Materia) {
        viewModelScope.launch {
            _materiasState.value = DataState.Loading
            try {
                val result = firestoreService.createMateria(userId, semestreId, materia)
                result.fold(
                    onSuccess = { materiaId ->
                        loadMaterias(userId, semestreId) // Reload materias
                    },
                    onFailure = { exception ->
                        _materiasState.value = DataState.Error(exception.message ?: "Error al crear materia")
                    }
                )
            } catch (e: Exception) {
                _materiasState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createMateriaWithEvaluaciones(
        userId: String, 
        semestreId: String, 
        materia: Materia,
        configuracionEvaluaciones: ConfiguracionEvaluaciones?
    ) {
        viewModelScope.launch {
            _materiasState.value = DataState.Loading
            try {
                // Primero crear la materia
                val result = firestoreService.createMateria(userId, semestreId, materia)
                result.fold(
                    onSuccess = { materiaId ->
                        // Luego crear las evaluaciones automáticamente si hay configuración
                        configuracionEvaluaciones?.let { config ->
                            // Obtener fecha actual para fechas por defecto
                            val calendar = Calendar.getInstance()
                            val currentYear = calendar.get(Calendar.YEAR)
                            val currentMonth = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH es 0-based
                            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
                            
                            // Fechas por defecto basadas en la fecha actual
                            val defaultParcial1 = String.format("%02d/%02d/%04d", currentDay, currentMonth, currentYear)
                            val defaultParcial2 = String.format("%02d/%02d/%04d", currentDay, (currentMonth + 1).takeIf { it <= 12 } ?: 1, currentYear)
                            val defaultParcial3 = String.format("%02d/%02d/%04d", currentDay, (currentMonth + 2).takeIf { it <= 12 } ?: 1, currentYear)
                            val defaultOrdinario = String.format("%02d/%02d/%04d", currentDay, (currentMonth + 2).takeIf { it <= 12 } ?: 1, currentYear)
                            
                            val evaluaciones = listOf(
                                Evaluacion(
                                    nombre = "Parcial 1",
                                    fecha = config.parcial1_fecha.ifEmpty { defaultParcial1 }
                                ),
                                Evaluacion(
                                    nombre = "Parcial 2",
                                    fecha = config.parcial2_fecha.ifEmpty { defaultParcial2 }
                                ),
                                Evaluacion(
                                    nombre = "Parcial 3",
                                    fecha = config.parcial3_fecha.ifEmpty { defaultParcial3 }
                                ),
                                Evaluacion(
                                    nombre = "Ordinario",
                                    fecha = config.ordinario_fecha.ifEmpty { defaultOrdinario }
                                )
                            )
                            
                            // Crear cada evaluación y programar notificación
                            evaluaciones.forEach { evaluacion ->
                                println("DEBUG: Creando evaluación automática: ${evaluacion.nombre} - Fecha: ${evaluacion.fecha}")
                                firestoreService.createEvaluacion(userId, semestreId, materiaId, evaluacion)
                                    .fold(
                                        onSuccess = { evaluacionId ->
                                            println("DEBUG: Evaluación automática creada: ${evaluacion.nombre} con ID: $evaluacionId")
                                            // Programar notificación para la evaluación
                                            println("DEBUG: NotificationScheduler disponible para evaluación automática: ${notificationScheduler != null}")
                                            println("DEBUG: Intentando programar notificación para: ${evaluacion.nombre}")
                                            try {
                                                notificationScheduler?.programarNotificacion(
                                                    evaluacion.copy(id = evaluacionId),
                                                    materia,
                                                    semestreId
                                                )
                                                println("DEBUG: Llamada a programarNotificacion completada para: ${evaluacion.nombre}")
                                            } catch (e: Exception) {
                                                println("DEBUG: ERROR al programar notificación: ${e.message}")
                                                e.printStackTrace()
                                            }
                                            println("DEBUG: Notificación programada para evaluación automática: ${evaluacion.nombre}")
                                        },
                                        onFailure = { exception ->
                                            println("DEBUG: ERROR al crear evaluación automática ${evaluacion.nombre}: ${exception.message}")
                                        }
                                    )
                            }
                        }
                        
                        loadMaterias(userId, semestreId) // Reload materias
                    },
                    onFailure = { exception ->
                        _materiasState.value = DataState.Error(exception.message ?: "Error al crear materia")
                    }
                )
            } catch (e: Exception) {
                _materiasState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun updateMateria(userId: String, semestreId: String, materia: Materia) {
        viewModelScope.launch {
            _materiasState.value = DataState.Loading
            try {
                println("DEBUG: Actualizando materia - ID: ${materia.id}, Nombre: ${materia.nombre}")
                val result = firestoreService.updateMateria(userId, semestreId, materia)
                result.fold(
                    onSuccess = {
                        println("DEBUG: Materia actualizada exitosamente")
                        loadMaterias(userId, semestreId) // Reload materias
                    },
                    onFailure = { exception ->
                        println("DEBUG: Error al actualizar materia: ${exception.message}")
                        _materiasState.value = DataState.Error(exception.message ?: "Error al actualizar materia")
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Excepción al actualizar materia: ${e.message}")
                _materiasState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun deleteMateria(userId: String, semestreId: String, materiaId: String) {
        viewModelScope.launch {
            _materiasState.value = DataState.Loading
            try {
                val result = firestoreService.deleteMateria(userId, semestreId, materiaId)
                result.fold(
                    onSuccess = {
                        loadMaterias(userId, semestreId) // Reload materias
                    },
                    onFailure = { exception ->
                        _materiasState.value = DataState.Error(exception.message ?: "Error al eliminar materia")
                    }
                )
            } catch (e: Exception) {
                _materiasState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    // Evaluacion operations
    fun loadEvaluaciones(userId: String, semestreId: String, materiaId: String) {
        viewModelScope.launch {
            _evaluacionesState.value = DataState.Loading
            try {
                val result = firestoreService.getEvaluaciones(userId, semestreId, materiaId)
                result.fold(
                    onSuccess = { evaluaciones ->
                        _evaluaciones.value = evaluaciones
                        _evaluacionesState.value = DataState.Success
                    },
                    onFailure = { exception ->
                        _evaluacionesState.value = DataState.Error(exception.message ?: "Error al cargar evaluaciones")
                    }
                )
            } catch (e: Exception) {
                _evaluacionesState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun createEvaluacion(userId: String, semestreId: String, materiaId: String, evaluacion: Evaluacion) {
        viewModelScope.launch {
            _evaluacionesState.value = DataState.Loading
            try {
                println("DEBUG: Creando evaluación: ${evaluacion.nombre} - Fecha: ${evaluacion.fecha}")
                val result = firestoreService.createEvaluacion(userId, semestreId, materiaId, evaluacion)
                result.fold(
                    onSuccess = { evaluacionId ->
                        println("DEBUG: Evaluación creada exitosamente con ID: $evaluacionId")
                        // Programar notificación para la nueva evaluación
                        val materia = materias.value.find { it.id == materiaId }
                        if (materia != null) {
                            println("DEBUG: Materia encontrada para notificación: ${materia.nombre}")
                            println("DEBUG: NotificationScheduler disponible: ${notificationScheduler != null}")
                            println("DEBUG: Intentando programar notificación para evaluación manual: ${evaluacion.nombre}")
                            try {
                                notificationScheduler?.programarNotificacion(
                                    evaluacion.copy(id = evaluacionId),
                                    materia,
                                    semestreId
                                )
                                println("DEBUG: Llamada a programarNotificacion completada para evaluación manual: ${evaluacion.nombre}")
                            } catch (e: Exception) {
                                println("DEBUG: ERROR al programar notificación manual: ${e.message}")
                                e.printStackTrace()
                            }
                            println("DEBUG: Notificación programada para evaluación: ${evaluacion.nombre}")
                        } else {
                            println("DEBUG: ERROR - No se encontró la materia con ID: $materiaId")
                        }
                        loadEvaluaciones(userId, semestreId, materiaId) // Reload evaluaciones
                    },
                    onFailure = { exception ->
                        println("DEBUG: ERROR al crear evaluación: ${exception.message}")
                        _evaluacionesState.value = DataState.Error(exception.message ?: "Error al crear evaluación")
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: EXCEPCIÓN al crear evaluación: ${e.message}")
                _evaluacionesState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun updateEvaluacion(userId: String, semestreId: String, materiaId: String, evaluacion: Evaluacion) {
        viewModelScope.launch {
            _evaluacionesState.value = DataState.Loading
            try {
                println("DEBUG: Actualizando evaluación: ${evaluacion.nombre} - Fecha: ${evaluacion.fecha}")
                val result = firestoreService.updateEvaluacion(userId, semestreId, materiaId, evaluacion)
                result.fold(
                    onSuccess = {
                        println("DEBUG: Evaluación actualizada exitosamente: ${evaluacion.nombre}")
                        // Cancelar notificación anterior
                        println("DEBUG: Cancelando notificación anterior para: ${evaluacion.nombre}")
                        notificationScheduler?.cancelarNotificacion(evaluacion.id)
                        
                        // Solo programar notificación si la evaluación no tiene calificación y la fecha es futura o de hoy
                        if (evaluacion.calificacionResultado <= 0) {
                            val materia = materias.value.find { it.id == materiaId }
                            if (materia != null) {
                                println("DEBUG: Materia encontrada para notificación de actualización: ${materia.nombre}")
                                println("DEBUG: NotificationScheduler disponible para actualización: ${notificationScheduler != null}")
                                println("DEBUG: Evaluación sin calificación, verificando fecha para notificación: ${evaluacion.nombre}")
                                
                                // Verificar si la fecha es futura o de hoy
                                val evaluacionDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                    .parse(evaluacion.fecha)
                                val now = java.util.Calendar.getInstance()
                                val evaluacionCalendar = java.util.Calendar.getInstance().apply {
                                    time = evaluacionDate ?: now.time
                                }
                                
                                val esMismoDia = evaluacionCalendar.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                                       evaluacionCalendar.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
                                val esFutura = evaluacionCalendar.after(now)
                                
                                if (esMismoDia || esFutura) {
                                    println("DEBUG: Fecha válida para notificación (hoy o futura), programando: ${evaluacion.nombre}")
                                    try {
                                        println("DEBUG: NotificationScheduler antes de llamar (updateEvaluacion): ${notificationScheduler}")
                                        println("DEBUG: NotificationScheduler es null (updateEvaluacion): ${notificationScheduler == null}")
                                        notificationScheduler?.programarNotificacion(evaluacion, materia, semestreId)
                                        println("DEBUG: Llamada a programarNotificacion completada para evaluación actualizada: ${evaluacion.nombre}")
                                        println("DEBUG: Notificación programada para evaluación actualizada: ${evaluacion.nombre}")
                                    } catch (e: Exception) {
                                        println("DEBUG: ERROR al programar notificación de actualización: ${e.message}")
                                        e.printStackTrace()
                                    }
                                } else {
                                    println("DEBUG: Fecha ya pasó, no se programa notificación para: ${evaluacion.nombre}")
                                }
                            } else {
                                println("DEBUG: ERROR - No se encontró la materia con ID: $materiaId para actualización")
                            }
                        } else {
                            println("DEBUG: Evaluación ya tiene calificación (${evaluacion.calificacionResultado}), no se programa notificación para: ${evaluacion.nombre}")
                        }
                        loadEvaluaciones(userId, semestreId, materiaId) // Reload evaluaciones
                    },
                    onFailure = { exception ->
                        println("DEBUG: ERROR al actualizar evaluación: ${exception.message}")
                        _evaluacionesState.value = DataState.Error(exception.message ?: "Error al actualizar evaluación")
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: EXCEPCIÓN al actualizar evaluación: ${e.message}")
                _evaluacionesState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun deleteEvaluacion(userId: String, semestreId: String, materiaId: String, evaluacionId: String) {
        viewModelScope.launch {
            _evaluacionesState.value = DataState.Loading
            try {
                // Cancelar notificación antes de eliminar
                notificationScheduler?.cancelarNotificacion(evaluacionId)
                
                val result = firestoreService.deleteEvaluacion(userId, semestreId, materiaId, evaluacionId)
                result.fold(
                    onSuccess = {
                        loadEvaluaciones(userId, semestreId, materiaId) // Reload evaluaciones
                    },
                    onFailure = { exception ->
                        _evaluacionesState.value = DataState.Error(exception.message ?: "Error al eliminar evaluación")
                    }
                )
            } catch (e: Exception) {
                _evaluacionesState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    // Selection management
    fun setCurrentSemestre(semestreId: String?) {
        _currentSemestreId.value = semestreId
        _currentMateriaId.value = null // Reset materia selection
        _materias.value = emptyList()
        _evaluaciones.value = emptyList()
    }

    fun setCurrentMateria(materiaId: String?) {
        _currentMateriaId.value = materiaId
        _evaluaciones.value = emptyList()
    }

    fun clearError() {
        if (_semestresState.value is DataState.Error) {
            _semestresState.value = DataState.Initial
        }
        if (_materiasState.value is DataState.Error) {
            _materiasState.value = DataState.Initial
        }
        if (_evaluacionesState.value is DataState.Error) {
            _evaluacionesState.value = DataState.Initial
        }
    }

    fun updateFechasEvaluacionesDeSemestre(userId: String, semestreId: String, config: ConfiguracionEvaluaciones) {
        viewModelScope.launch {
            try {
                println("DEBUG: Iniciando actualización de fechas para semestre: $semestreId")
                
                // Obtener todas las materias del semestre
                val materiasResult = firestoreService.getMaterias(userId, semestreId)
                materiasResult.fold(
                    onSuccess = { materias ->
                        println("DEBUG: Encontradas ${materias.size} materias para actualizar")
                        
                        // Actualizar evaluaciones de cada materia
                        materias.forEach { materia ->
                            println("DEBUG: Actualizando evaluaciones de materia: ${materia.nombre}")
                            
                            val evalsResult = firestoreService.getEvaluaciones(userId, semestreId, materia.id)
                            evalsResult.fold(
                                onSuccess = { evaluaciones ->
                                    var evaluacionesActualizadas = 0
                                    
                                    evaluaciones.forEach { evaluacion ->
                                        // Solo actualizar si el nombre coincide
                                        val nuevaFecha = when (evaluacion.nombre) {
                                            "Parcial 1" -> config.parcial1_fecha
                                            "Parcial 2" -> config.parcial2_fecha
                                            "Parcial 3" -> config.parcial3_fecha
                                            "Ordinario" -> config.ordinario_fecha
                                            else -> evaluacion.fecha
                                        }
                                        
                                        if (nuevaFecha.isNotEmpty() && nuevaFecha != evaluacion.fecha) {
                                            println("DEBUG: Actualizando ${evaluacion.nombre} de '${evaluacion.fecha}' a '$nuevaFecha'")
                                            
                                            val evaluacionActualizada = evaluacion.copy(fecha = nuevaFecha)
                                            val updateResult = firestoreService.updateEvaluacion(userId, semestreId, materia.id, evaluacionActualizada)
                                            
                                            updateResult.fold(
                                                onSuccess = {
                                                    evaluacionesActualizadas++
                                                    println("DEBUG: Evaluación ${evaluacion.nombre} actualizada exitosamente")
                                                    
                                                    // Cancelar notificación anterior y programar nueva
                                                    println("DEBUG: Cancelando notificación anterior para actualización masiva: ${evaluacion.nombre}")
                                                    notificationScheduler?.cancelarNotificacion(evaluacion.id)
                                                    
                                                    // Solo programar notificación si la evaluación no tiene calificación y la fecha es futura o de hoy
                                                    if (evaluacionActualizada.calificacionResultado <= 0) {
                                                        println("DEBUG: Evaluación sin calificación, verificando fecha para notificación masiva: ${evaluacion.nombre}")
                                                        
                                                        // Verificar si la fecha es futura o de hoy
                                                        val evaluacionDate = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                                                            .parse(evaluacionActualizada.fecha)
                                                        val now = java.util.Calendar.getInstance()
                                                        val evaluacionCalendar = java.util.Calendar.getInstance().apply {
                                                            time = evaluacionDate ?: now.time
                                                        }
                                                        
                                                        val esMismoDia = evaluacionCalendar.get(java.util.Calendar.YEAR) == now.get(java.util.Calendar.YEAR) &&
                                                               evaluacionCalendar.get(java.util.Calendar.DAY_OF_YEAR) == now.get(java.util.Calendar.DAY_OF_YEAR)
                                                        val esFutura = evaluacionCalendar.after(now)
                                                        
                                                        if (esMismoDia || esFutura) {
                                                            println("DEBUG: Fecha válida para notificación masiva (hoy o futura), programando: ${evaluacion.nombre}")
                                                            println("DEBUG: Intentando programar notificación para actualización masiva: ${evaluacion.nombre}")
                                                            try {
                                                                println("DEBUG: NotificationScheduler antes de llamar: ${notificationScheduler}")
                                                                println("DEBUG: NotificationScheduler es null: ${notificationScheduler == null}")
                                                                notificationScheduler?.programarNotificacion(evaluacionActualizada, materia, semestreId)
                                                                println("DEBUG: Llamada a programarNotificacion completada para actualización masiva: ${evaluacion.nombre}")
                                                                println("DEBUG: Notificación programada para actualización masiva: ${evaluacion.nombre}")
                                                            } catch (e: Exception) {
                                                                println("DEBUG: ERROR al programar notificación de actualización masiva: ${e.message}")
                                                                e.printStackTrace()
                                                            }
                                                        } else {
                                                            println("DEBUG: Fecha ya pasó, no se programa notificación masiva para: ${evaluacion.nombre}")
                                                        }
                                                    } else {
                                                        println("DEBUG: Evaluación ya tiene calificación (${evaluacionActualizada.calificacionResultado}), no se programa notificación masiva para: ${evaluacion.nombre}")
                                                    }
                                                },
                                                onFailure = { exception ->
                                                    println("DEBUG: Error al actualizar evaluación ${evaluacion.nombre}: ${exception.message}")
                                                }
                                            )
                                        }
                                    }
                                    
                                    println("DEBUG: Se actualizaron $evaluacionesActualizadas evaluaciones en materia ${materia.nombre}")
                                    
                                    // Recargar evaluaciones de esta materia específica
                                    loadEvaluaciones(userId, semestreId, materia.id)
                                },
                                onFailure = { exception ->
                                    println("DEBUG: Error al obtener evaluaciones de materia ${materia.nombre}: ${exception.message}")
                                }
                            )
                        }
                        
                        // Recargar todas las materias del semestre después de actualizar
                        println("DEBUG: Recargando materias del semestre")
                        loadMaterias(userId, semestreId)
                        
                        println("DEBUG: Actualización de fechas completada exitosamente")
                    },
                    onFailure = { exception ->
                        println("DEBUG: Error al obtener materias del semestre: ${exception.message}")
                    }
                )
            } catch (e: Exception) {
                println("DEBUG: Excepción durante actualización de fechas: ${e.message}")
            }
        }
    }

    fun existeSemestreConNombre(nombre: String): Boolean {
        return semestres.value.any { it.nombre.trim().equals(nombre.trim(), ignoreCase = true) }
    }

    fun existeMateriaConNombre(nombre: String): Boolean {
        return materias.value.any { it.nombre.trim().equals(nombre.trim(), ignoreCase = true) }
    }

    // Métodos para validaciones y cálculos
    fun validarEvaluacionesPendientes(userId: String, semestreId: String, materiaId: String): List<EvaluacionPendiente> {
        val evaluaciones = _evaluaciones.value
        val evaluacionesPendientes = mutableListOf<EvaluacionPendiente>()
        
        // Separar evaluaciones por tipo
        val parciales = evaluaciones.filter { it.nombre.contains("Parcial", ignoreCase = true) }
        val ordinario = evaluaciones.find { it.nombre.contains("Ordinario", ignoreCase = true) }
        
        // Verificar parciales sin calificación
        parciales.forEach { parcial ->
            if (parcial.calificacionResultado <= 0) {
                val fechaEvaluacion = parseFecha(parcial.fecha)
                val hoy = Calendar.getInstance()
                
                when {
                    fechaEvaluacion.before(hoy) -> {
                        evaluacionesPendientes.add(
                            EvaluacionPendiente(
                                evaluacion = parcial,
                                tipo = TipoPendiente.FECHA_VENCIDA,
                                mensaje = "La fecha de ${parcial.nombre} ya pasó. Debes registrar tu calificación."
                            )
                        )
                    }
                    esMismoDia(fechaEvaluacion, hoy) -> {
                        evaluacionesPendientes.add(
                            EvaluacionPendiente(
                                evaluacion = parcial,
                                tipo = TipoPendiente.HOY,
                                mensaje = "Hoy es ${parcial.nombre}. ¡No olvides registrar tu calificación!"
                            )
                        )
                    }
                    fechaEvaluacion.after(hoy) -> {
                        evaluacionesPendientes.add(
                            EvaluacionPendiente(
                                evaluacion = parcial,
                                tipo = TipoPendiente.FUTURA,
                                mensaje = "${parcial.nombre} será el ${parcial.fecha}. Prepárate bien."
                            )
                        )
                    }
                }
            }
        }
        
        // Verificar ordinario sin calificación
        ordinario?.let { ord ->
            if (ord.calificacionResultado <= 0) {
                val fechaEvaluacion = parseFecha(ord.fecha)
                val hoy = Calendar.getInstance()
                
                when {
                    fechaEvaluacion.before(hoy) -> {
                        evaluacionesPendientes.add(
                            EvaluacionPendiente(
                                evaluacion = ord,
                                tipo = TipoPendiente.FECHA_VENCIDA,
                                mensaje = "La fecha del Ordinario ya pasó. Debes registrar tu calificación."
                            )
                        )
                    }
                    esMismoDia(fechaEvaluacion, hoy) -> {
                        evaluacionesPendientes.add(
                            EvaluacionPendiente(
                                evaluacion = ord,
                                tipo = TipoPendiente.HOY,
                                mensaje = "Hoy es el Ordinario. ¡No olvides registrar tu calificación!"
                            )
                        )
                    }
                    fechaEvaluacion.after(hoy) -> {
                        evaluacionesPendientes.add(
                            EvaluacionPendiente(
                                evaluacion = ord,
                                tipo = TipoPendiente.FUTURA,
                                mensaje = "El Ordinario será el ${ord.fecha}. Prepárate bien."
                            )
                        )
                    }
                }
            }
        }
        
        return evaluacionesPendientes
    }

    fun calcularCalificacionNecesaria(evaluacion: Evaluacion, materia: Materia): Double {
        val evaluaciones = _evaluaciones.value
        
        // Separar evaluaciones por tipo
        val parciales = evaluaciones.filter { it.nombre.contains("Parcial", ignoreCase = true) }
        val ordinario = evaluaciones.find { it.nombre.contains("Ordinario", ignoreCase = true) }
        
        // Filtrar evaluaciones completadas
        val parcialesCompletados = parciales.filter { it.calificacionResultado > 0 }
        val ordinarioCompletado = ordinario?.takeIf { it.calificacionResultado > 0 }
        
        val resultado = when {
            evaluacion.nombre.contains("Parcial", ignoreCase = true) -> {
                calcularCalificacionParcial(materia, evaluacion, parciales, ordinarioCompletado)
            }
            evaluacion.nombre.contains("Ordinario", ignoreCase = true) -> {
                calcularCalificacionOrdinario(materia, parcialesCompletados)
            }
            else -> materia.calificacionObjetivo
        }
        
        // Limitar entre 0 y 10
        return maxOf(0.0, minOf(10.0, resultado))
    }

    fun calcularMaximaCalificacionAlcanzable(evaluacion: Evaluacion, materia: Materia): Double {
        val evaluaciones = _evaluaciones.value
        
        // Separar evaluaciones por tipo
        val parciales = evaluaciones.filter { it.nombre.contains("Parcial", ignoreCase = true) }
        val ordinario = evaluaciones.find { it.nombre.contains("Ordinario", ignoreCase = true) }
        
        // Filtrar evaluaciones completadas
        val parcialesCompletados = parciales.filter { it.calificacionResultado > 0 }
        val ordinarioCompletado = ordinario?.takeIf { it.calificacionResultado > 0 }
        
        return when {
            evaluacion.nombre.contains("Parcial", ignoreCase = true) -> {
                calcularMaximaCalificacionParcial(materia, evaluacion, parciales, ordinarioCompletado)
            }
            evaluacion.nombre.contains("Ordinario", ignoreCase = true) -> {
                calcularMaximaCalificacionOrdinario(materia, parcialesCompletados)
            }
            else -> 10.0
        }
    }

    fun sePuedeAlcanzarObjetivo(evaluacion: Evaluacion, materia: Materia): Boolean {
        val calificacionNecesaria = calcularCalificacionNecesaria(evaluacion, materia)
        return calificacionNecesaria <= 10.0
    }

    private fun calcularCalificacionParcial(
        materia: Materia,
        evaluacionActual: Evaluacion,
        todosLosParciales: List<Evaluacion>,
        ordinarioCompletado: Evaluacion?
    ): Double {
        // Filtrar parciales completados (excluyendo el actual)
        val parcialesCompletados = todosLosParciales.filter { 
            it.calificacionResultado > 0 && it.id != evaluacionActual.id 
        }
        
        return if (ordinarioCompletado != null) {
            // Si ya hay ordinario, calcular para que el promedio de parciales alcance el objetivo
            val promedioParcialesActual = if (parcialesCompletados.isNotEmpty()) {
                parcialesCompletados.map { it.calificacionResultado }.average()
            } else 0.0
            
            // Fórmula corregida: (Objetivo - (Ordinario * 0.5)) / 0.5 = Promedio necesario de parciales
            val promedioNecesario = (materia.calificacionObjetivo - (ordinarioCompletado.calificacionResultado * 0.5)) / 0.5
            val parcialesRestantes = 3 - parcialesCompletados.size
            
            if (parcialesRestantes > 0) {
                val sumaNecesaria = promedioNecesario * 3
                val sumaActual = parcialesCompletados.sumOf { it.calificacionResultado }
                val calificacionNecesaria = (sumaNecesaria - sumaActual) / parcialesRestantes
                
                // Si la calificación necesaria supera 10.0, calcular la máxima posible
                if (calificacionNecesaria > 10.0) {
                    // Calcular la máxima calificación posible
                    val sumaMaxima = (promedioParcialesActual * parcialesCompletados.size) + (10.0 * parcialesRestantes)
                    val promedioMaximo = sumaMaxima / 3
                    val calificacionMaxima = (promedioMaximo * 0.5) + (ordinarioCompletado.calificacionResultado * 0.5)
                    
                    // Si la máxima calificación posible es menor al objetivo, calcular para alcanzar la máxima
                    if (calificacionMaxima < materia.calificacionObjetivo) {
                        val promedioParaMaxima = (calificacionMaxima - (ordinarioCompletado.calificacionResultado * 0.5)) / 0.5
                        val sumaParaMaxima = promedioParaMaxima * 3
                        val calificacionParaMaxima = (sumaParaMaxima - sumaActual) / parcialesRestantes
                        maxOf(0.0, minOf(10.0, calificacionParaMaxima))
                    } else {
                        // Si aún se puede alcanzar el objetivo con 10.0
                        maxOf(0.0, minOf(10.0, calificacionNecesaria))
                    }
                } else {
                    calificacionNecesaria
                }
            } else {
                materia.calificacionObjetivo
            }
        } else {
            // Si no hay ordinario, calcular para que el promedio de parciales alcance el objetivo
            val promedioNecesario = materia.calificacionObjetivo
            val parcialesRestantes = 3 - parcialesCompletados.size
            
            if (parcialesRestantes > 0) {
                val sumaNecesaria = promedioNecesario * 3
                val sumaActual = parcialesCompletados.sumOf { it.calificacionResultado }
                val calificacionNecesaria = (sumaNecesaria - sumaActual) / parcialesRestantes
                
                // Si la calificación necesaria supera 10.0, calcular la máxima posible
                if (calificacionNecesaria > 10.0) {
                    // Calcular la máxima calificación posible
                    val promedioParcialesActual = if (parcialesCompletados.isNotEmpty()) {
                        parcialesCompletados.map { it.calificacionResultado }.average()
                    } else 0.0
                    
                    val sumaMaxima = (promedioParcialesActual * parcialesCompletados.size) + (10.0 * parcialesRestantes)
                    val promedioMaximo = sumaMaxima / 3
                    val calificacionMaxima = promedioMaximo * 0.5 + 5.0 // Asumiendo 5.0 en ordinario
                    
                    // Calcular para alcanzar la máxima calificación posible
                    val promedioParaMaxima = calificacionMaxima * 2 // Convertir de vuelta
                    val sumaParaMaxima = promedioParaMaxima * 3
                    val calificacionParaMaxima = (sumaParaMaxima - sumaActual) / parcialesRestantes
                    maxOf(0.0, minOf(10.0, calificacionParaMaxima))
                } else {
                    calificacionNecesaria
                }
            } else {
                materia.calificacionObjetivo
            }
        }
    }

    private fun calcularCalificacionOrdinario(
        materia: Materia,
        parcialesCompletados: List<Evaluacion>
    ): Double {
        // Si no hay parciales completados, el ordinario necesita el objetivo
        if (parcialesCompletados.isEmpty()) {
            return materia.calificacionObjetivo
        }
        
        val promedioParciales = parcialesCompletados.map { it.calificacionResultado }.average()
        
        // Fórmula corregida: (Objetivo - (PromedioParciales * 0.5)) / 0.5 = Calificación necesaria en ordinario
        val calificacionNecesaria = (materia.calificacionObjetivo - (promedioParciales * 0.5)) / 0.5
        
        // Si la calificación necesaria supera 10.0, calcular la máxima posible
        if (calificacionNecesaria > 10.0) {
            // La máxima calificación posible es con 10.0 en ordinario
            val calificacionMaxima = (promedioParciales * 0.5) + (10.0 * 0.5)
            return 10.0 // Para alcanzar la máxima posible
        }
        
        // Si la calificación necesaria es menor a 0, significa que los parciales ya garantizan el objetivo
        if (calificacionNecesaria < 0) {
            return 0.0
        }
        
        return calificacionNecesaria
    }

    private fun calcularMaximaCalificacionParcial(
        materia: Materia,
        evaluacionActual: Evaluacion,
        todosLosParciales: List<Evaluacion>,
        ordinarioCompletado: Evaluacion?
    ): Double {
        val parcialesCompletados = todosLosParciales.filter { 
            it.calificacionResultado > 0 && it.id != evaluacionActual.id 
        }
        
        return if (ordinarioCompletado != null) {
            // Con ordinario completado, calcular la máxima calificación posible
            val promedioParcialesActual = if (parcialesCompletados.isNotEmpty()) {
                parcialesCompletados.map { it.calificacionResultado }.average()
            } else 0.0
            
            val parcialesRestantes = 3 - parcialesCompletados.size
            if (parcialesRestantes > 0) {
                // Si saca 10 en todos los parciales restantes
                val sumaMaxima = (promedioParcialesActual * parcialesCompletados.size) + (10.0 * parcialesRestantes)
                val promedioMaximo = sumaMaxima / 3
                val calificacionMaxima = (promedioMaximo * 0.5) + (ordinarioCompletado.calificacionResultado * 0.5)
                calificacionMaxima
            } else {
                (promedioParcialesActual * 0.5) + (ordinarioCompletado.calificacionResultado * 0.5)
            }
        } else {
            // Sin ordinario, la máxima calificación posible es 10.0
            10.0
        }
    }

    private fun calcularMaximaCalificacionOrdinario(
        materia: Materia,
        parcialesCompletados: List<Evaluacion>
    ): Double {
        val promedioParciales = if (parcialesCompletados.isNotEmpty()) {
            parcialesCompletados.map { it.calificacionResultado }.average()
        } else 0.0
        
        // Si saca 10 en el ordinario
        return (promedioParciales * 0.5) + (10.0 * 0.5)
    }

    private fun parseFecha(fecha: String): Calendar {
        return try {
            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(fecha)
            Calendar.getInstance().apply { time = date ?: Date() }
        } catch (e: Exception) {
            Calendar.getInstance()
        }
    }

    private fun esMismoDia(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}

sealed class DataState {
    object Initial : DataState()
    object Loading : DataState()
    object Success : DataState()
    data class Error(val message: String) : DataState()
}

// Clases de datos para validaciones
data class EvaluacionPendiente(
    val evaluacion: Evaluacion,
    val tipo: TipoPendiente,
    val mensaje: String
)

enum class TipoPendiente {
    FECHA_VENCIDA,
    HOY,
    FUTURA
} 