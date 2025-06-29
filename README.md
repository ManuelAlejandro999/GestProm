# GestProm - Firebase Backend Integration

GestProm es una aplicación Android para la gestión de promedios académicos que ahora incluye integración completa con Firebase para autenticación y almacenamiento de datos.

## Características

- **Autenticación con Firebase**: Login y registro de usuarios
- **Firestore Database**: Almacenamiento en la nube de todos los datos
- **Estructura de datos jerárquica**: Usuarios → Semestres → Materias → Evaluaciones
- **Sincronización en tiempo real**: Los datos se sincronizan automáticamente
- **Interfaz moderna**: Diseño Material 3 con Jetpack Compose

## Estructura de Datos en Firestore

```
usuarios (colección)
└── {usuarioId} (documento)
    ├── nombre_completo
    ├── matricula
    ├── correo
    ├── contraseña_hash
    └── semestres (subcolección)
        └── {semestreId} (documento)
            ├── nombre
            ├── configuracionEvaluaciones (mapa)
            │   ├── parcial1_fecha
            │   ├── parcial2_fecha
            │   ├── parcial3_fecha
            │   └── ordinario_fecha
            └── materias (subcolección)
                └── {materiaId} (documento)
                    ├── nombre
                    ├── calificacion_objetivo
                    └── evaluaciones (subcolección)
                        └── {evaluacionId} (documento)
                            ├── nombre (ej. "Parcial 1")
                            ├── fecha (copiada de configuración)
                            ├── calificacion_resultado
                            ├── fecha_registro
```

## Configuración de Firebase

### 1. Crear proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Crea un nuevo proyecto
3. Habilita Authentication con Email/Password
4. Crea una base de datos Firestore en modo de prueba

### 2. Configurar Android App

1. En Firebase Console, agrega una aplicación Android
2. Usa el package name: `com.example.gestprom`
3. Descarga el archivo `google-services.json`
4. Colócalo en la carpeta `app/` del proyecto

### 3. Configurar reglas de Firestore

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuarios solo pueden acceder a sus propios datos
    match /usuarios/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      
      // Semestres del usuario
      match /semestres/{semestreId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
        
        // Materias del semestre
        match /materias/{materiaId} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
          
          // Evaluaciones de la materia
          match /evaluaciones/{evaluacionId} {
            allow read, write: if request.auth != null && request.auth.uid == userId;
          }
        }
      }
    }
  }
}
```

## Arquitectura de la Aplicación

### Modelos de Datos

- **Usuario**: Información del usuario autenticado
- **Semestre**: Período académico con configuración de evaluaciones
- **Materia**: Asignatura con calificación objetivo
- **Evaluacion**: Calificación individual con fecha y resultado

### Servicios

- **AuthService**: Maneja autenticación con Firebase Auth
- **FirestoreService**: Operaciones CRUD en Firestore

### ViewModels

- **AuthViewModel**: Estado de autenticación y operaciones de login/registro
- **DataViewModel**: Gestión de datos (semestres, materias, evaluaciones)

### Pantallas

- **ScreenLogin**: Inicio de sesión con Firebase Auth
- **ScreenRegistro**: Registro de nuevos usuarios
- **ScreenSemestres**: Gestión de semestres con Firestore
- **ScreenMaterias**: Gestión de materias por semestre
- **ScreenEvaluaciones**: Gestión de evaluaciones por materia

## Funcionalidades Implementadas

### Autenticación
- ✅ Login con email/contraseña
- ✅ Registro de nuevos usuarios
- ✅ Logout
- ✅ Persistencia de sesión
- ✅ Validación de credenciales

### Gestión de Semestres
- ✅ Crear semestres
- ✅ Editar nombres de semestres
- ✅ Listar semestres del usuario
- ✅ Eliminar semestres

### Gestión de Materias
- ✅ Crear materias en un semestre
- ✅ Editar información de materias
- ✅ Listar materias por semestre
- ✅ Eliminar materias

### Gestión de Evaluaciones
- ✅ Crear evaluaciones en una materia
- ✅ Editar calificaciones
- ✅ Listar evaluaciones por materia
- ✅ Eliminar evaluaciones

## Dependencias

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
implementation("com.google.firebase:firebase-analytics-ktx")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")

// ViewModel y Lifecycle
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")
```

## Uso

### 1. Registro de Usuario
1. Abre la aplicación
2. Toca "CREAR CUENTA"
3. Completa el formulario con:
   - Nombre completo
   - Matrícula (mínimo 6 caracteres)
   - Correo electrónico válido
   - Contraseña (mínimo 6 caracteres)
4. Toca "CREAR CUENTA"

### 2. Inicio de Sesión
1. En la pantalla de login, ingresa:
   - Matrícula (se convierte automáticamente a email)
   - Contraseña
2. Toca "INICIAR SESIÓN"

### 3. Gestión de Semestres
1. Desde el menú principal, toca "Semestres"
2. Toca "Añadir Semestre" para crear uno nuevo
3. Toca el icono de opciones para editar o eliminar

### 4. Gestión de Materias
1. Toca un semestre para ver sus materias
2. Añade materias con nombre y calificación objetivo
3. Configura fechas de evaluaciones

### 5. Gestión de Evaluaciones
1. Toca una materia para ver sus evaluaciones
2. Añade evaluaciones con calificaciones
3. El sistema calcula automáticamente el promedio

## Seguridad

- Autenticación requerida para todas las operaciones
- Usuarios solo pueden acceder a sus propios datos
- Contraseñas hasheadas con SHA-256
- Validación de entrada en cliente y servidor

## Próximas Mejoras

- [ ] Sincronización offline
- [ ] Notificaciones push para fechas de evaluaciones
- [ ] Exportación de datos a PDF
- [ ] Gráficos de progreso académico
- [ ] Backup automático de datos
- [ ] Modo oscuro/claro
- [ ] Soporte para múltiples idiomas

## Contribución

1. Fork el proyecto
2. Crea una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abre un Pull Request

## Licencia

Este proyecto está bajo la Licencia MIT. Ver el archivo `LICENSE` para más detalles.

## Contacto

Manuel Alejandro - [@ManuelAlejandro999](https://github.com/ManuelAlejandro999)

Link del proyecto: [https://github.com/ManuelAlejandro999/GestProm.git](https://github.com/ManuelAlejandro999/GestProm.git) 