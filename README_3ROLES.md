# PagoFlex - Versión con 3 roles funcionales (basada en Cristian + arreglos de Main)

## Diseño
Se mantiene **íntegramente el diseño y colores de la rama Cristian**:
- Verde primario: `#17A083` (PagoFlexGreen)
- Azul secundario: `#2355D4` (PagoFlexBlue)
- Componentes, Dimens y tema originales.

## Roles funcionales

| Rol        | Usuario de prueba              | Contraseña | Destino al login      |
|------------|--------------------------------|------------|-----------------------|
| CLIENTE    | sofia@gmail.com / 20.435.214-5 | cli123     | UsuarioScreen         |
| AGENTE     | matias@pagoflex.com            | age123     | AgenteScreen          |
| EJECUTIVO  | carla@empresa.com / 12.345.678-9 | eje123   | EjecutivoScreen (nuevo) |

## Cambios realizados (arreglos portados de Main)

1. **Base de datos** (`PagoFlexDbHelper.kt`)
   - Versión BD = 18
   - Rol EJECUTIVO siempre presente
   - Usuario demo "Carla Ejecutivo"
   - Compromiso de ejemplo para cobranza
   - Corregido el hardcode `rolNombre = "CLIENTE"` al listar compromisos (ahora usa el JOIN real con la tabla roles)
   - Migración segura en onUpgrade para versiones anteriores

2. **Navegación** (`Rutas.kt` + `AppNavegacion.kt`)
   - Nueva ruta `Ruta.Ejecutivo`
   - `rutaParaRol` reconoce CLIENTE / USUARIO_FINAL / AGENTE / EJECUTIVO
   - Carga de compromisos: el Ejecutivo ve **todos** los de la empresa; los demás solo los propios

3. **Nueva pantalla** `EjecutivoScreen.kt`
   - Dashboard con el mismo estilo visual de Cristian
   - Resumen de cobranza (pendientes / pagados / montos)
   - Listado de compromisos de la empresa
   - Botones: Registrar compromiso, Mi perfil, Cerrar sesión
   - Usa componentes existentes: TarjetaEstadistica, TarjetaSeccion, TarjetaCompromiso, BotonPrincipal, BotonSecundario

## Cómo abrir en Android Studio

1. Descomprime el ZIP.
2. Abre la carpeta `ApplicacionMovil-dev-cristian` como proyecto en Android Studio.
3. Espera a que Gradle sincronice (si falta el wrapper jar, Android Studio lo descarga).
4. Ejecuta en un emulador o dispositivo.
5. Prueba los 3 logins de la tabla de arriba.
6. **Importante**: si ya tenías la app instalada con una BD antigua, desinstálala primero o borra los datos de la app para que se cree la BD con versión 18 y los usuarios demo.

## Notas

- La pantalla de Ejecutivo es funcional para ver cobranza y navegar. Se puede seguir ampliando (registro completo de compromisos como en Main, anulación, etc.) manteniendo siempre el diseño de Cristian.
- El resto de pantallas (Cliente y Agente) no se modificaron visualmente.
