package com.example.gestprom.services

import com.example.gestprom.models.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class FirestoreService {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    // Usuario operations
    suspend fun createUser(usuario: Usuario): Result<String> {
        return try {
            val docRef = db.collection("usuarios").document(usuario.id)
            docRef.set(usuario).await()
            Result.success(usuario.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<Usuario?> {
        return try {
            val document = db.collection("usuarios").document(userId).get().await()
            if (document.exists()) {
                val usuario = document.toObject(Usuario::class.java)
                Result.success(usuario)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(usuario: Usuario): Result<Unit> {
        return try {
            db.collection("usuarios").document(usuario.id).set(usuario).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Semestre operations
    suspend fun createSemestre(userId: String, semestre: Semestre): Result<String> {
        return try {
            val docRef = db.collection("usuarios").document(userId)
                .collection("semestres").document()
            val semestreWithId = semestre.copy(id = docRef.id)
            docRef.set(semestreWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSemestres(userId: String): Result<List<Semestre>> {
        return try {
            val snapshot = db.collection("usuarios").document(userId)
                .collection("semestres").get().await()
            val semestres = snapshot.documents.mapNotNull { doc ->
                val semestre = doc.toObject(Semestre::class.java)?.copy(id = doc.id)
                semestre
            }
            Result.success(semestres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSemestre(userId: String, semestre: Semestre): Result<Unit> {
        return try {
            db.collection("usuarios").document(userId)
                .collection("semestres").document(semestre.id).set(semestre).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSemestre(userId: String, semestreId: String): Result<Unit> {
        return try {
            db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Materia operations
    suspend fun createMateria(userId: String, semestreId: String, materia: Materia): Result<String> {
        return try {
            val docRef = db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document()
            val materiaWithId = materia.copy(id = docRef.id)
            docRef.set(materiaWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMaterias(userId: String, semestreId: String): Result<List<Materia>> {
        return try {
            println("DEBUG: FirestoreService - Obteniendo materias")
            println("DEBUG: Path: usuarios/$userId/semestres/$semestreId/materias")
            
            val snapshot = db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").get().await()
            
            val materias = snapshot.documents.mapNotNull { doc ->
                val materia = doc.toObject(Materia::class.java)?.copy(id = doc.id)
                println("DEBUG: Materia encontrada - ID: ${materia?.id}, Nombre: ${materia?.nombre}")
                materia
            }
            
            println("DEBUG: Total de materias obtenidas: ${materias.size}")
            Result.success(materias)
        } catch (e: Exception) {
            println("DEBUG: Error en FirestoreService al obtener materias: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateMateria(userId: String, semestreId: String, materia: Materia): Result<Unit> {
        return try {
            println("DEBUG: FirestoreService - Actualizando materia en Firestore")
            println("DEBUG: Path: usuarios/$userId/semestres/$semestreId/materias/${materia.id}")
            println("DEBUG: Materia a actualizar: $materia")
            
            db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materia.id).set(materia).await()
            
            println("DEBUG: Materia actualizada exitosamente en Firestore")
            // Log extra: leer el documento actualizado
            val doc = db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materia.id).get().await()
            println("DEBUG: Documento Firestore tras update: ${doc.data}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("DEBUG: Error en FirestoreService al actualizar materia: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteMateria(userId: String, semestreId: String, materiaId: String): Result<Unit> {
        return try {
            db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materiaId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Evaluacion operations
    suspend fun createEvaluacion(
        userId: String, 
        semestreId: String, 
        materiaId: String, 
        evaluacion: Evaluacion
    ): Result<String> {
        return try {
            val docRef = db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materiaId)
                .collection("evaluaciones").document()
            val evaluacionWithId = evaluacion.copy(
                id = docRef.id,
                fechaRegistro = dateFormat.format(Date())
            )
            docRef.set(evaluacionWithId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getEvaluaciones(
        userId: String, 
        semestreId: String, 
        materiaId: String
    ): Result<List<Evaluacion>> {
        return try {
            val snapshot = db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materiaId)
                .collection("evaluaciones").get().await()
            val evaluaciones = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Evaluacion::class.java)?.copy(id = doc.id)
            }
            Result.success(evaluaciones)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateEvaluacion(
        userId: String, 
        semestreId: String, 
        materiaId: String, 
        evaluacion: Evaluacion
    ): Result<Unit> {
        return try {
            db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materiaId)
                .collection("evaluaciones").document(evaluacion.id).set(evaluacion).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteEvaluacion(
        userId: String, 
        semestreId: String, 
        materiaId: String, 
        evaluacionId: String
    ): Result<Unit> {
        return try {
            db.collection("usuarios").document(userId)
                .collection("semestres").document(semestreId)
                .collection("materias").document(materiaId)
                .collection("evaluaciones").document(evaluacionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 