# PagoFlex

Aplicación Android para consultar y gestionar compromisos de pago. Incluye acceso para clientes, agentes de atención y ejecutivos de empresa.

## Tecnologías

- Kotlin, Jetpack Compose y Material 3.
- ViewModel y StateFlow para el estado de las pantallas.
- SQLiteOpenHelper para los datos locales y SharedPreferences para la sesión.
- WorkManager para recordatorios de vencimiento.
- JUnit para las pruebas unitarias.

## Funciones

- Inicio de sesión único y navegación según el rol de la cuenta.
- Registro de clientes con validaciones de RUT, correo y contraseña.
- Consulta de servicios y compromisos, con búsqueda y filtros por estado.
- Pago del compromiso y consulta de su comprobante.
- Cuotas mensuales y aplazamiento para servicios externos; el ejecutivo puede registrar y modificar cuotas de compromisos de su empresa.
- Envío de reportes, atención por agentes y consulta de las respuestas.
- Foto de perfil con cámara o galería y recordatorios de vencimiento configurables.

## Ejecutar el proyecto

1. Abrir esta carpeta en Android Studio y esperar la sincronización de Gradle.
2. Seleccionar un emulador o dispositivo Android con API 24 o superior.
3. Ejecutar la configuración `app`.
4. Para las pruebas unitarias, ejecutar la tarea Gradle `testDebugUnitTest`.

## Estado actual y trabajo pendiente

Los datos, usuarios, reportes y pagos se guardan en SQLite del dispositivo. Los importes automáticos de servicios externos son datos locales de demostración; no provienen de una empresa ni de una API. Los pagos no cobran dinero real. Un cambio de dispositivo no transfiere estos datos.

La pantalla de recuperación valida el correo, pero el envío y verificación del código y el cambio de clave aún necesitan conectarse a un microservicio. También queda completar el avance de foco y revisar las validaciones de todos los formularios, ampliar las animaciones y conectar las operaciones y notificaciones que correspondan al backend. No se deben solicitar ni guardar datos de tarjetas bancarias.

La base de datos cuenta con migraciones para conservar los datos al actualizar la aplicación. Para comprobar el trabajo del equipo, usar commits descriptivos y repartir las tareas entre integrantes; consultar el historial de Git para conocer la participación real.
