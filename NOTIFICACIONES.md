# Sistema de Notificaciones - GestProm

## Descripción

El sistema de notificaciones de GestProm te permite recibir recordatorios automáticos el día de tus evaluaciones, incluyendo la calificación que necesitas obtener para alcanzar tu calificación objetivo.

## Características

### 📅 Notificaciones Automáticas
- **Cuándo**: El día de cada evaluación a las 8:00 AM
- **Contenido**: 
  - Nombre de la materia y evaluación
  - Calificación necesaria para alcanzar tu objetivo
  - Al tocar la notificación se abre la app

### 🧮 Cálculo de Calificación Necesaria
El sistema calcula automáticamente la calificación que necesitas basándose en:
- **Parciales (50%)**: Promedio de los 3 parciales
- **Ordinario (50%)**: Calificación del examen ordinario
- **Fórmula**: 
  - Si ya tienes ordinario: `(Objetivo - (Ordinario × 0.5)) ÷ 0.5`
  - Si no tienes ordinario: `(Objetivo - (Promedio Parciales × 0.5)) ÷ 0.5`

### ⚙️ Gestión de Notificaciones
- **Programación automática**: Al crear o actualizar evaluaciones
- **Cancelación automática**: Al eliminar evaluaciones
- **Reprogramación**: Botón para reprogramar todas las notificaciones
- **Vista previa**: Pantalla dedicada para ver evaluaciones programadas

## Configuración

### Permisos Requeridos
- **Android 13+**: Permiso de notificaciones (`POST_NOTIFICATIONS`)
- **Android <13**: No requiere permisos adicionales

### Inicialización
El sistema se inicializa automáticamente cuando:
1. El usuario inicia sesión
2. Se cargan las evaluaciones existentes
3. Se crean nuevas evaluaciones

## Uso

### 1. Acceder a Notificaciones
- Desde el menú principal, toca "📅 Notificaciones"
- Selecciona un semestre y materia
- Ve las evaluaciones programadas

### 2. Reprogramar Notificaciones
- En la pantalla de notificaciones
- Toca "Reprogramar Todas las Notificaciones"
- Útil si cambiaste fechas o reinstalaste la app

### 3. Gestión Automática
- **Crear evaluación**: Se programa notificación automáticamente
- **Actualizar fecha**: Se cancela la anterior y programa la nueva
- **Eliminar evaluación**: Se cancela la notificación automáticamente

## Estructura Técnica

### Componentes Principales
- `NotificationService`: Worker que ejecuta las notificaciones
- `NotificationScheduler`: Clase para programar/cancelar notificaciones
- `DataViewModel`: Integración con el sistema de datos
- `ScreenNotificaciones`: UI para gestionar notificaciones

### WorkManager
- Usa WorkManager para programar notificaciones exactas
- Persiste a través de reinicios del dispositivo
- Maneja automáticamente las restricciones del sistema

### Firebase Integration
- Obtiene datos actualizados de Firestore
- Calcula calificaciones en tiempo real
- Sincroniza con el estado actual de evaluaciones

## Ejemplo de Notificación

```
📅 ¡Evaluación Hoy!
Matemáticas - Parcial 2
Necesitas sacar 8.5 para alcanzar tu calificación objetivo
```

## Solución de Problemas

### Notificaciones no aparecen
1. Verifica permisos en Configuración > Apps > GestProm > Notificaciones
2. Asegúrate de que la fecha de evaluación sea futura
3. Usa "Reprogramar Todas las Notificaciones"

### Cálculo incorrecto
1. Verifica que las calificaciones estén actualizadas
2. Confirma que la calificación objetivo esté configurada
3. Revisa que los nombres de evaluaciones sean correctos (Parcial 1, Parcial 2, etc.)

### Notificaciones duplicadas
1. Usa "Reprogramar Todas las Notificaciones" para limpiar
2. Verifica que no haya evaluaciones duplicadas
3. Reinicia la app si es necesario

## Notas Técnicas

- Las notificaciones se programan para las 8:00 AM del día de la evaluación
- El sistema maneja automáticamente las zonas horarias del dispositivo
- Las notificaciones persisten a través de reinicios y actualizaciones
- El cálculo se realiza en tiempo real usando datos de Firestore 