# Configuración de Firebase para GestProm

## Pasos para configurar Firebase correctamente:

### 1. Crear proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en "Crear un proyecto"
3. Dale un nombre como "GestProm"
4. Puedes deshabilitar Google Analytics por ahora
5. Haz clic en "Crear proyecto"

### 2. Configurar Authentication

1. En el panel izquierdo, haz clic en "Authentication"
2. Haz clic en "Comenzar"
3. En la pestaña "Sign-in method", habilita "Email/Password"
4. Haz clic en "Guardar"

### 3. Configurar Firestore Database

1. En el panel izquierdo, haz clic en "Firestore Database"
2. Haz clic en "Crear base de datos"
3. Selecciona "Comenzar en modo de prueba" (para desarrollo)
4. Selecciona una ubicación cercana (ej: us-central1)
5. Haz clic en "Siguiente"

### 4. Configurar reglas de Firestore

1. En Firestore Database, ve a la pestaña "Reglas"
2. Reemplaza las reglas existentes con:

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

3. Haz clic en "Publicar"

### 5. Agregar aplicación Android

1. En la página principal del proyecto, haz clic en el ícono de Android
2. Ingresa el package name: `com.example.gestprom`
3. Puedes saltar el apodo de la app
4. Haz clic en "Registrar app"
5. Descarga el archivo `google-services.json`
6. Coloca el archivo en la carpeta `app/` de tu proyecto

### 6. Verificar configuración

1. Asegúrate de que el archivo `google-services.json` esté en `app/google-services.json`
2. Verifica que el plugin de Google Services esté en `app/build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.google.gms.google-services")
   }
   ```
3. Verifica que las dependencias de Firebase estén en `app/build.gradle.kts`:
   ```kotlin
   implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
   implementation("com.google.firebase:firebase-analytics-ktx")
   implementation("com.google.firebase:firebase-auth-ktx")
   implementation("com.google.firebase:firebase-firestore-ktx")
   ```

### 7. Probar la aplicación

1. Compila y ejecuta la aplicación
2. Registra un nuevo usuario con un email válido
3. Cierra sesión
4. Intenta iniciar sesión con las mismas credenciales

### 8. Solución de problemas comunes

#### Error: "Contraseña incorrecta"
- Verifica que el email esté escrito exactamente igual
- Asegúrate de que la contraseña tenga al menos 6 caracteres
- Verifica que no haya espacios extra

#### Error: "Usuario no encontrado"
- Verifica que el usuario se haya registrado correctamente
- Revisa la consola de Firebase para ver si hay errores
- Verifica que el email esté en el formato correcto

#### Error: "Error de conexión"
- Verifica tu conexión a internet
- Asegúrate de que Firebase esté configurado correctamente
- Revisa que las reglas de Firestore permitan acceso

### 9. Verificar en Firebase Console

1. Ve a Authentication > Users para ver usuarios registrados
2. Ve a Firestore Database > Data para ver los documentos creados
3. Verifica que los datos se estén guardando correctamente

### 10. Logs de debug

Si sigues teniendo problemas, revisa los logs de Android Studio para ver errores específicos de Firebase. 