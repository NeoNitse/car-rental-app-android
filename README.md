<div align="center">
  <img src="https://readme-typing-svg.demolab.com/?font=Racing+Sans+One&weight=700&size=50&pause=1000&color=D32F2F&center=true&vCenter=true&width=600&lines=Horizon+Rides;Alquiler+de+Veh%C3%ADculos" alt="Horizon Rides Animado" />
</div>

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-UI-4285F4?logo=jetpackcompose&logoColor=white)
![Firebase](https://img.shields.io/badge/Firebase-Auth%20%7C%20Firestore-FFCA28?logo=firebase&logoColor=black)

*Aplicación Android nativa para la gestión de alquiler de vehículos, desarrollada como proyecto académico.*
</div>

```

## Manual de Usuario

La app funciona distinto según si entrás como cliente o como recepcionista.

### Cliente

1. Creá una cuenta con tu correo y contraseña, o iniciá sesión si ya tenés una.
2. En la pantalla principal aparece el catálogo de vehículos disponibles. Tocá cualquiera para ver sus detalles y el precio por día.
3. Elegí el vehículo que querés alquilar y seleccioná la fecha de inicio y de devolución en el calendario. El costo total se calcula solo, según los días que elegiste.
4. Antes de mandar la solicitud, la app te pide una foto de tu DUI o licencia. Un lector OCR revisa que el documento sea legible.
5. Enviá la solicitud. El vehículo queda en estado `PENDIENTE` hasta que un recepcionista la revise.
6. Te llega una notificación cuando la solicitud es aprobada o rechazada. Si fue aprobada, podés ver el estado de tu alquiler en tu perfil.

### Recepcionista

1. Iniciá sesión con tu cuenta de recepcionista.
2. El panel principal muestra las solicitudes pendientes de revisión.
3. Revisá los datos del cliente y el documento que subió antes de aprobar o rechazar la solicitud.
4. Al aprobar, el vehículo cambia de `PENDIENTE` a `EN_PROCESO`, y pasa a `EN_USO` cuando el cliente lo retira.
5. Cuando el cliente devuelve el vehículo, marcalo como devuelto para que vuelva a estado `DISPONIBLE`.
6. Desde el mismo panel se puede administrar el inventario: agregar vehículos nuevos, editarlos o darlos de baja.

## Descarga Oficial

descargar el APK directamente desde nuestra página web de Google Play:
**[Sitio Web Oficial de Horizon Rides](https://neonitse.github.io/horizon-rides-web/)**

## Descripción del proyecto

**Horizon Rides** es un sistema de alquiler de autos con sincronización en la nube. La aplicación maneja dos tipos de usuarios:
1. **Clientes:** Pueden registrarse, explorar el catálogo, validar su identidad usando Inteligencia Artificial y enviar solicitudes de alquiler.
2. **Recepcionistas:** Tienen el control total del inventario, pueden aprobar o rechazar solicitudes en tiempo real y hacer seguimiento de los autos retirados y devueltos.

## Características principales

- **Sistema de Roles:** Inicio de sesión y registro gestionado mediante Firebase Authentication, separando las vistas de Cliente y Recepción.
- **Base de Datos en la Nube:** Todo el inventario, usuarios y reservas se sincronizan en tiempo real gracias a Firebase Firestore.
- **Validación con Inteligencia Artificial:** Integración de Google ML Kit (OCR) para escanear y validar automáticamente los documentos de identidad (DUI o Licencia) al momento de reservar.
- **Notificaciones en Vivo:** Los clientes reciben alertas en su pantalla principal en cuanto un recepcionista aprueba su solicitud.
- **Flujo de Estados:** Máquina de estados estricta para los vehículos (`DISPONIBLE → PENDIENTE → EN_PROCESO → EN_USO → DISPONIBLE`).
- **Cálculo de Costos:** Calculadora automática del total a pagar según las fechas seleccionadas en el calendario.

## Stack Tecnológico

| Tecnología | Uso en el proyecto |
|---|---|
| **Kotlin** | Lenguaje de programación principal. |
| **Jetpack Compose** | Creación de toda la interfaz de usuario (UI declarativa). |
| **Firebase Auth** | Registro y autenticación de usuarios. |
| **Firebase Firestore** | Base de datos NoSQL en la nube en tiempo real. |
| **Google ML Kit** | Reconocimiento óptico de caracteres (OCR) para validar documentos. |
| **Corrutinas / Flow** | Manejo de tareas asíncronas y programación reactiva. |

## Arquitectura

El proyecto está estructurado utilizando **MVVM (Model-View-ViewModel)** junto con principios de **Clean Architecture** para mantener el código ordenado y fácil de mantener:
- **UI (Presentación):** Pantallas en Compose que solo observan estados.
- **Domain (Casos de Uso):** La lógica de negocio separada en acciones específicas (ej. RentCarUseCase, ApproveRentalUseCase).
- **Data (Repositorios):** Conexión directa con Firebase Firestore para leer y escribir datos de forma remota.

## Instalación y ejecución

1. Clona el repositorio:
   ```bash
   git clone [https://github.com/NeoNitse/car-rental-app-android.git](https://github.com/NeoNitse/car-rental-app-android.git)
