#  El Veloz — Sistema de Alquiler de Vehículos

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-BOM%202026.02.01-4285F4?logo=jetpackcompose&logoColor=white)
![Room](https://img.shields.io/badge/Room-2.7.1-3DDC84?logo=android&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-26-blue)

Aplicación Android nativa para la gestión de alquiler de vehículos
## Índice

- [Descripción](#-descripción-del-proyecto)
- [Características principales](#-características-principales)
- [Arquitectura](#️-arquitectura)
- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos previos](#️-requisitos-previos)
- [Instalación y ejecución](#️-instalación-y-ejecución)
- [Manual de uso](#-manual-de-uso)
- [Reglas de negocio](#-reglas-de-negocio)
- [Patrones de UI/UX aplicados](#-patrones-de-uiux-aplicados)
- [Capturas de pantalla](#-capturas-de-pantalla)
- [Autor](#-autor)

##  Descripción del proyecto

"El Veloz" simula el sistema de un mostrador de alquiler de autos: el personal de recepción puede consultar el inventario, registrar una reserva a nombre de un cliente, hacer seguimiento del ciclo de vida del contrato (reservado → retirado → devuelto) y calcular automáticamente el costo total según los días de alquiler.

##  Características principales

- **Catálogo de 8 vehículos** precargados automáticamente en la base de datos local al primer arranque.
- **Máquina de estados** con transición estricta: `DISPONIBLE → EN_PROCESO → EN_USO → DISPONIBLE`.
- **Cálculo automático de costos**: días de alquiler × precio por día, con un mínimo de 1 día.
- **Búsqueda en vivo** por marca o modelo.
- **Filtros rápidos** por estado del vehículo (Todos / Disponibles / En Proceso / En Uso).
- **Panel de estadísticas** con conteo en tiempo real por estado, siempre sobre el inventario completo sin importar el filtro activo.
- **Línea de tiempo visual (Stepper)** para seguir el progreso de una reserva de un vistazo.
- **Selección de fechas** mediante `DatePicker` nativo de Material 3, sin campos de texto libre.
- **Cancelación de reservas** antes del retiro físico del vehículo.
- **Validaciones**: nombre de cliente obligatorio, fechas obligatorias y bloqueo de fecha de entrega anterior a la de recogida.

##  Arquitectura

El proyecto sigue **Clean Architecture** con el patrón **MVVM** y flujo unidireccional de datos (UDF), dividido en tres capas:

```
┌───────────────────────────────────────────────┐
│           CAPA DE PRESENTACIÓN (UI)            │
│   Jetpack Compose + ViewModel + StateFlow      │
└──────────────────────┬──────────────────────────┘
                       │ consume UI State
                       ▼
┌───────────────────────────────────────────────┐
│            CAPA DE DOMINIO (Domain)            │
│    UseCases + Repository (interfaz) + Model    │
└──────────────────────┬──────────────────────────┘
                       │ lógica de negocio
                       ▼
┌───────────────────────────────────────────────┐
│             CAPA DE DATOS (Data)               │
│   RepositoryImpl + Room Database (DAO/Entity)  │
└───────────────────────────────────────────────┘
```

- **UI**: `CatalogScreen` (Compose) observa `CatalogViewModel` a través de `StateFlow`; no contiene lógica de negocio.
- **Domain**: 7 casos de uso encapsulan cada operación de negocio de forma aislada y testeable.
- **Data**: `CarRepositoryImpl` implementa la interfaz `CarRepository`, delegando en `CarDao` y `RentalDao` (Room).

### Casos de uso

| Caso de uso | Responsabilidad |
|---|---|
| `GetCarsUseCase` | Expone el catálogo completo como `Flow` reactivo |
| `CalculateCostUseCase` | Calcula el costo total y valida el rango de fechas |
| `RentCarUseCase` | Crea la reserva y cambia el auto a `EN_PROCESO` |
| `UpdateCarStatusUseCase` | Actualiza el estado del vehículo (ej. a `EN_USO`) |
| `GetActiveRentalUseCase` | Obtiene la reserva activa asociada a un vehículo |
| `CompleteRentalUseCase` | Cierra la reserva y libera el vehículo a `DISPONIBLE` |
| `CancelRentalUseCase` | Elimina una reserva antes del retiro y libera el vehículo |

##  Stack tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Kotlin | 2.0.0 | Lenguaje principal |
| Jetpack Compose | BOM 2026.02.01 | UI declarativa |
| Material 3 | — | Componentes visuales y `DatePicker` |
| Room | 2.7.1 | Persistencia local (SQLite) |
| KSP | 2.0.0-1.0.24 | Procesamiento de anotaciones de Room |
| Kotlin Coroutines / Flow | — | Concurrencia y reactividad (`StateFlow`, `combine`) |
| AGP | 9.2.1 | Android Gradle Plugin |

##  Estructura del proyecto

```
com.sv.elveloz/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── CarDao.kt
│   │   │   └── RentalDao.kt
│   │   ├── entity/
│   │   │   ├── CarEntity.kt
│   │   │   └── RentalEntity.kt
│   │   ├── AppDatabase.kt
│   │   └── Converters.kt
│   └── repository/
│       └── CarRepositoryImpl.kt
├── domain/
│   ├── model/
│   │   └── CarStatus.kt
│   ├── repository/
│   │   └── CarRepository.kt
│   └── usecase/
│       ├── GetCarsUseCase.kt
│       ├── CalculateCostUseCase.kt
│       ├── RentCarUseCase.kt
│       ├── UpdateCarStatusUseCase.kt
│       ├── GetActiveRentalUseCase.kt
│       ├── CompleteRentalUseCase.kt
│       └── CancelRentalUseCase.kt
├── ui/
│   ├── catalog/
│   │   ├── CatalogScreen.kt
│   │   ├── CatalogViewModel.kt
│   │   └── CatalogUiState.kt
│   ├── theme/
│   ├── AppViewModelFactory.kt
│   └── MainActivity.kt
```

##  Requisitos previos

- **Android Studio** con soporte para AGP 9.2.1 y Kotlin 2.0.0 (versión reciente).
- **JDK 11** o superior.
- **Android SDK** con API 26 (Android 8.0) como mínimo y API 36 como target.
- Conexión a internet para la primera sincronización de Gradle (descarga de dependencias).
- Emulador o dispositivo físico con Android 8.0 o superior.

## ▶ Instalación y ejecución

1. Cloná el repositorio:
   ```bash
   git clone https://github.com/NeoNitse/car-rental-app-android.git
   ```
2. Abrí la carpeta del proyecto en Android Studio.
3. Esperá a que Gradle sincronice automáticamente (descarga las dependencias declaradas en `libs.versions.toml`).
4. Seleccioná un emulador o conectá un dispositivo físico con API 26+.
5. Ejecutá con el botón **Run ▶** o `Shift + F10`.

La base de datos se crea y se precarga automáticamente con los 8 vehículos la primera vez que se ejecuta la app — no requiere ninguna configuración manual adicional.


##  Manual de uso

### 1. Catálogo principal
Al abrir la app se muestra el panel de estadísticas (autos disponibles / en proceso / en uso), la barra de búsqueda, los filtros rápidos y la lista de los 8 vehículos con marca, modelo, precio por día y estado actual.

### 2. Buscar y filtrar
- Escribí en la barra de búsqueda para filtrar por marca o modelo en tiempo real.
- Tocá cualquiera de los chips (`Todos`, `Disponibles`, `En Proceso`, `En Uso`) para filtrar la lista por estado. El panel de estadísticas de arriba siempre refleja el inventario completo, sin importar el filtro activo.

### 3. Registrar una reserva
1. Tocá cualquier vehículo con estado **Disponible**.
2. Completá el nombre del cliente.
3. Seleccioná la fecha de recogida y la fecha de entrega con el selector de calendario.
4. Tocá **Confirmar Reserva**.
5. El vehículo cambia automáticamente a **En Proceso** y el costo total se calcula solo.

### 4. Marcar retiro del vehículo
1. Tocá un vehículo en estado **En Proceso**.
2. Revisá los datos de la reserva (cliente, fechas, costo total) y el avance en el stepper visual.
3. Tocá **Marcar como Retirado** — el estado pasa a **En Uso**.

### 5. Registrar devolución
1. Tocá un vehículo en estado **En Uso**.
2. Tocá **Marcar como Devuelto** — el vehículo vuelve a **Disponible** y la reserva queda cerrada.

### 6. Cancelar una reserva
Si una reserva fue registrada por error, mientras el vehículo esté **En Proceso** (aún no retirado físicamente) podés tocarlo y elegir **Cancelar Reserva**. El vehículo vuelve a **Disponible** de inmediato y la reserva se elimina. Esta opción deja de estar disponible una vez que el vehículo pasa a **En Uso**.

##  Reglas de negocio

- El inventario es fijo: siempre son 8 vehículos, no se pueden agregar ni eliminar desde la app.
- Un vehículo solo puede reservarse si su estado es `DISPONIBLE`.
- El costo se calcula como `días de alquiler × precio por día`, con un mínimo de 1 día aunque las fechas de recogida y entrega coincidan.
- El sistema rechaza cualquier reserva donde la fecha de entrega sea anterior a la fecha de recogida, mostrando un mensaje de error sin cerrar el formulario.
- Cancelar una reserva solo es posible mientras el vehículo está `EN_PROCESO`; una vez retirado (`EN_USO`), la única acción disponible es marcarlo como devuelto.

##  Patrones de UI/UX aplicados

1. **Personalización según contexto** — panel de estadísticas rápidas en la pantalla principal, siempre visible.
2. **Búsqueda inteligente** — filtro en vivo combinado con chips de filtrado rápido por estado.
3. **Seguimiento visual de estados** — stepper de 3 pasos con colores semánticos en el detalle de cada reserva.
4. **Consistencia visual** — tarjetas uniformes con iconografía y fondos pastel según el estado del vehículo.
5. **Inputs adecuados** — `DatePicker` nativo de Material 3 en lugar de campos de texto libre para las fechas.

##  Capturas de pantalla

*(Agregar acá las capturas del catálogo, el diálogo de reserva y el detalle con el stepper. Guardalas en una carpeta `/screenshots` dentro del repositorio y referencialas así:)*

```markdown
![Catálogo](screenshots/catalogo.png)
![Reserva](screenshots/reserva.png)
![Detalle con stepper](screenshots/detalle.png)
```

## 🎓 Contexto académico

| | |
|---|---|
| Materia | Desarrollo de Aplicaciones Móviles II |
| Institución | Universidad Modular Abierta |
| Docente | Ing. Herberth Contreras |
| Evaluación | Laboratorio 1 |
| Fecha de entrega | 18 de agosto de 2026 |
