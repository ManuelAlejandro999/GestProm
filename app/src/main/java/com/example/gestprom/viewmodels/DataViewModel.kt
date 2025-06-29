package com.example.gestprom.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gestprom.models.*
import com.example.gestprom.services.FirestoreService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataViewModel : ViewModel() {
    private val firestoreService = FirestoreService()

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

    // Semestre operations
    fun loadSemestres(userId: String) {
        viewModelScope.launch {
            _semestresState.value = DataState.Loading
            try {
                val result = firestoreService.getSemestres(userId)
                result.fold(
                    onSuccess = { semestres ->
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
                val result = firestoreService.getMaterias(userId, semestreId)
                result.fold(
                    onSuccess = { materias ->
                        _materias.value = materias
                        _materiasState.value = DataState.Success
                    },
                    onFailure = { exception ->
                        _materiasState.value = DataState.Error(exception.message ?: "Error al cargar materias")
                    }
                )
            } catch (e: Exception) {
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
                            val evaluaciones = listOf(
                                Evaluacion(
                                    nombre = "Parcial 1",
                                    fecha = config.parcial1_fecha.ifEmpty { "15/02/2026" }
                                ),
                                Evaluacion(
                                    nombre = "Parcial 2",
                                    fecha = config.parcial2_fecha.ifEmpty { "15/03/2026" }
                                ),
                                Evaluacion(
                                    nombre = "Parcial 3",
                                    fecha = config.parcial3_fecha.ifEmpty { "15/04/2026" }
                                ),
                                Evaluacion(
                                    nombre = "Ordinario",
                                    fecha = config.ordinario_fecha.ifEmpty { "25/04/2026" }
                                )
                            )
                            
                            // Crear cada evaluación
                            evaluaciones.forEach { evaluacion ->
                                firestoreService.createEvaluacion(userId, semestreId, materiaId, evaluacion)
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
                val result = firestoreService.updateMateria(userId, semestreId, materia)
                result.fold(
                    onSuccess = {
                        loadMaterias(userId, semestreId) // Reload materias
                    },
                    onFailure = { exception ->
                        _materiasState.value = DataState.Error(exception.message ?: "Error al actualizar materia")
                    }
                )
            } catch (e: Exception) {
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
                val result = firestoreService.createEvaluacion(userId, semestreId, materiaId, evaluacion)
                result.fold(
                    onSuccess = { evaluacionId ->
                        loadEvaluaciones(userId, semestreId, materiaId) // Reload evaluaciones
                    },
                    onFailure = { exception ->
                        _evaluacionesState.value = DataState.Error(exception.message ?: "Error al crear evaluación")
                    }
                )
            } catch (e: Exception) {
                _evaluacionesState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun updateEvaluacion(userId: String, semestreId: String, materiaId: String, evaluacion: Evaluacion) {
        viewModelScope.launch {
            _evaluacionesState.value = DataState.Loading
            try {
                val result = firestoreService.updateEvaluacion(userId, semestreId, materiaId, evaluacion)
                result.fold(
                    onSuccess = {
                        loadEvaluaciones(userId, semestreId, materiaId) // Reload evaluaciones
                    },
                    onFailure = { exception ->
                        _evaluacionesState.value = DataState.Error(exception.message ?: "Error al actualizar evaluación")
                    }
                )
            } catch (e: Exception) {
                _evaluacionesState.value = DataState.Error(e.message ?: "Error inesperado")
            }
        }
    }

    fun deleteEvaluacion(userId: String, semestreId: String, materiaId: String, evaluacionId: String) {
        viewModelScope.launch {
            _evaluacionesState.value = DataState.Loading
            try {
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
            // Obtener todas las materias del semestre
            val materiasResult = firestoreService.getMaterias(userId, semestreId)
            materiasResult.fold(onSuccess = { materias ->
                materias.forEach { materia ->
                    // Obtener todas las evaluaciones de la materia
                    val evalsResult = firestoreService.getEvaluaciones(userId, semestreId, materia.id)
                    evalsResult.fold(onSuccess = { evaluaciones ->
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
                                val evaluacionActualizada = evaluacion.copy(fecha = nuevaFecha)
                                firestoreService.updateEvaluacion(userId, semestreId, materia.id, evaluacionActualizada)
                            }
                        }
                    }, onFailure = {})
                }
            }, onFailure = {})
        }
    }
}

sealed class DataState {
    object Initial : DataState()
    object Loading : DataState()
    object Success : DataState()
    data class Error(val message: String) : DataState()
} 