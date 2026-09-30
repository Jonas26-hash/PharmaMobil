# PharmaMobil

Aplicación multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** para la gestión de medicamentos, ventas y clientes de una cadena de boticas.

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin Multiplatform | Lógica compartida en `commonMain` (Android + iOS) |
| Compose Multiplatform | Interfaz de usuario declarativa |
| Ktor Client 3.5.2 | Cliente HTTP con Content Negotiation JSON, logging y timeouts |
| OkHttp / Darwin | Motores HTTP por plataforma (`OkHttp` en Android, `NSURLSession` en iOS) |
| kotlinx.serialization | Serialización de los DTO del contrato REST |
| Coroutines | Operaciones `suspend` reales contra el backend |
| StateFlow | Estados reactivos que refrescan la UI automáticamente |
| Clean Architecture | Capas `presentation → domain ← data` separadas y testables |
| Casos de uso | Reglas de negocio concentradas en `domain/usecase` |
| ViewModel | Estado de pantalla que sobrevive a cambios de configuración |
| Koin | Inyección de dependencias declarativa con módulos por capa |
| Material 3 | Tema propio de farmacia (verde) con modo claro y oscuro |

## Arquitectura

```
shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/
├── App.kt                          ← Punto de entrada Compose + drawer de navegación
├── theme/                          ← Paleta verde de farmacia (claro/oscuro)
├── navigation/                     ← Destinos de la navegación (Screen)
├── di/                             ← Módulos de Koin y arranque (initKoin)
├── domain/
│   ├── model/                      ← Producto, Cliente, Categoria, Venta, DetalleVenta
│   ├── repository/                 ← Contratos (ProductoRepository, ClienteRepository, CategoriaRepository)
│   └── usecase/                    ← Validación y lógica de negocio + Result<T>
├── data/
│   ├── remote/                     ← Cliente HTTP de Ktor: DTO, mappers, APIs, repositorios remotos
│   └── repository/                 ← Implementaciones en memoria (referencia para pruebas)
└── presentation/
    ├── components/                 ← EstadoVacio, MensajeExito, ValidatedTextField
    ├── inicio/                     ← Pantalla de inicio con accesos rápidos
    ├── producto/                   ← Listado con buscador + formulario de registro/edición
    └── cliente/                    ← Listado con buscador + formulario de registro/edición
```

Las dependencias apuntan hacia el centro: `presentation` usa casos de uso, `domain` define contratos y `data` los implementa. La UI nunca toca el repositorio directamente, y el dominio no conoce Ktor: los repositorios remotos traducen los fallos de red a `ErrorDeAplicacion`, que ya es un mensaje legible para el usuario.

## Dominio

### Producto

```kotlin
data class Producto(
    val id: Long,                    // lo asigna el repositorio
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double,
    val stock: Int,
    val requiereReceta: Boolean = false,
    val categoria: String = "",      // nombre que envía el backend
    val categoriaId: Long? = null,   // id real del catálogo
    val activo: Boolean = true
)
```

- Invariantes con `require()`: nombre no vacío, precio mayor que cero, stock no negativo.
- `requiereReposicion = activo && stock < STOCK_MINIMO` (mínimo de góndola = 10).

### Cliente

```kotlin
data class Cliente(
    val id: Long,
    val nombre: String,
    val apellido: String,
    val dni: String,                 // exactamente 8 dígitos
    val telefono: String? = null,
    val email: String? = null,
    val direccion: String? = null,
    val activo: Boolean = true       // columna "estado" del backend
)
```

- Propiedad calculada `nombreCompleto`.
- Teléfono, correo y dirección opcionales: no pueden ser cadenas vacías.

### Categoría

```kotlin
data class Categoria(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
)
```

- Catálogo que publica el backend (`GET /api/v1/categorias`) y que alimenta el selector del formulario de producto.

### Venta y DetalleVenta

- `DetalleVenta` calcula su `subtotal = cantidad * precioUnitario`.
- `Venta` suma los subtotales en `total` y descuenta stock al registrarse.

## Capa de datos

- `data/remote` habla con el backend REST (**pharmaSoft**, Spring Boot, contexto `/api/v1`):
  - `ConfigApi` es el **único** lugar donde vive la URL base y los parámetros de paginación.
  - `HttpClientFactory.kt` construye **un solo** `HttpClient` (Koin lo comparte como `single`): Content Negotiation JSON (`ignoreUnknownKeys`, `isLenient`, `encodeDefaults`, sin nulos explícitos), `Logging` en nivel `INFO`, timeouts (15 s / 10 s / 15 s), `defaultRequest` con la base y `expectSuccess = true`.
  - `crearMotorHttp()` es un `expect fun`: OkHttp en `androidMain`, Darwin en `iosMain`.
  - `ProductoApi`, `ClienteApi`, `CategoriaApi` y `VentaApi`: un método por endpoint, sin lógica de negocio.
  - `Dtos.kt` reproduce el contrato real (`PaginaResponseDto<T>`, `ProductoResponseDto`, `ClienteRequestDto`, `VentaRequestDto`…); `Mappers.kt` traduce DTO ↔ dominio.
  - `ProductoRepositorioRemoto`, `ClienteRepositorioRemoto` y `CategoriaRepositorioRemoto` implementan los contratos de `domain` y envuelven los fallos con `alAplicacion`.
- `MensajesDeError.kt` traduce cada fallo a un mensaje que el usuario pueda leer: 404, 400/422, 5xx, timeouts, error de serialización y falta de conexión. El orden importa: los timeouts heredan de `IOException`, así que se comprueban antes.
- `ProductoRepositorioEnMemoria` y `ClienteRepositorioEnMemoria` se conservan como referencia y soporte de pruebas, no como fuente de datos de la app.

## Casos de uso

- Producto: `RegistrarProductoUseCase`, `ActualizarProductoUseCase`, `EliminarProductoUseCase`, `ObtenerProductoUseCase`, `ListarProductosUseCase`.
- Cliente: `RegistrarClienteUseCase`, `ActualizarClienteUseCase`, `EliminarClienteUseCase`, `ObtenerClienteUseCase`, `ListarClientesUseCase`.
- `ListarCategoriasUseCase` y `RegistrarVentaUseCase(clienteId, productoId, cantidad)`.
- `ValidacionesProducto` y `ValidacionesCliente` concentran las reglas que se comparten entre registro y actualización.
- Todo se envuelve en `Result<T>` mediante `resultadoDe`, que relanza `CancellationException` para no interrumpir el flujo de una corrutina cancelada.

## Pantallas

| Pantalla | Contenido |
|---|---|
| **Inicio** | Bienvenida y accesos rápidos a productos, clientes y pedidos |
| **Productos** | Listado con buscador, precio formateado, aviso de reposición y botón de registro; al tocar un ítem se abre su edición |
| **Registrar / Editar producto** | Nombre, precio, stock, estado y selector de **categoría** (catálogo del backend); en edición, botón de eliminar |
| **Clientes** | Listado con buscador por nombre o DNI; al tocar un ítem se abre su edición |
| **Registrar / Editar cliente** | Nombre, apellido, DNI, teléfono, correo, dirección y estado; en edición, botón de eliminar |
| **Pedidos** | Módulo en construcción (estado vacío) |

- Navegación con **drawer único** en todas las resoluciones y barra superior verde. En los formularios, el ícono de la barra pasa a ser "volver".
- Los listados piden los datos con `LaunchedEffect(Unit)`, así que **cada visita refresca** el estado del servidor.
- Botón **Modo oscuro** en el drawer.
- Estados tipados en cada pantalla: `Cargando`, `SinProductos` / `SinClientes`, `ConProductos` / `ConClientes`, `Error` (solo uno activo, el compilador obliga a resolverlos con `when`).
- Validación visible por campo, mensajes de éxito (que además devuelven al listado) y estados vacíos con icono.

## Inyección de dependencias (Koin)

- `dataModule`: motor HTTP y `HttpClient` como `single` (uno solo en toda la app), APIs, repositorios remotos.
- `domainModule`: casos de uso (`factory`).
- `presentationModule`: los cuatro ViewModels con `viewModel { }` (dos de listado y dos de formulario).
- `initKoin { }` arranca Koin desde `MainApplication` (Android) y desde `iOSApp.swift` (iOS).

## Conectividad REST

PharmaMobil no inventa una URL pública: el backend es el repositorio **pharmaSoft** y debe ejecutarse en local.

1. Levantar la base de datos **Oracle** y luego el backend (Spring Boot, puerto `8080`, contexto `/api/v1`, usuario `PHARMADB` y clave `1234567`).

La forma más cómoda de tener Oracle es en Docker. Ojo con la versión: el esquema de pharmaSoft usa el tipo `boolean`, que existe desde **Oracle 23ai**, así que un 21c falla con `ORA-00902`.

```bash
docker run -d --name pharmadb -p 1522:1521 ^
  -e ORACLE_PASSWORD=1234567 -e APP_USER=PHARMADB -e APP_USER_PASSWORD=1234567 ^
  gvenzl/oracle-free:23-slim
```

El puerto 1522 es el que publica el contenedor; el 1521 es el interno. La primera vez descomprime los datafiles y tarda varios minutos: hasta que el log no muestre `DATABASE IS READY` el backend arrancará con `ORA-12541`. Necesita unos 2 GB de RAM libres. Con la base lista, en el repo de pharmaSoft:

```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev
```

2. Indicar la dirección del backend al compilar. No se edita `ConfigApi`: la URL se genera desde la propiedad de Gradle `pharmaApiBaseUrl` (tarea `generarUrlApi`), así la IP de tu máquina nunca queda en el repositorio.

| Plataforma | Comando |
|---|---|
| Emulador de Android | `.\gradlew :androidApp:installDebug` (por defecto: `http://10.0.2.2:8080/api/v1`) |
| Dispositivo físico Android | `.\gradlew :androidApp:installDebug -PpharmaApiBaseUrl=http://<IP-local-de-la-PC>:8080/api/v1` |
| Simulador de iOS | `-PpharmaApiBaseUrl=http://localhost:8080/api/v1` |

Para averiguar la IP local de la PC: `ipconfig`, sección del adaptador Wi-Fi.

> La IP cambia con frecuencia (DHCP). Si el listado muestra "No se pudo conectar con el servidor", casi siempre es que la IP cambió: vuelve a compilar con la nueva. La app no lleva la IP escrita a mano, por eso el cambio es solo recompilar.

3. En Android, el permiso `INTERNET` ya está declarado en el manifiesto. Además, `androidApp/src/main/res/xml/network_security_config.xml` habilita tráfico en claro, porque el backend local habla HTTP y Android 9+ lo bloquea por defecto. Ese archivo debe eliminarse si algún día el backend se sirve por HTTPS.

4. En Windows, el perfil de red "Público" puede bloquear el puerto 8080. Si la PC responde en `localhost` pero el teléfono no, hay que permitir Java (o el puerto 8080) en el Firewall de Defender para redes privadas.

Endpoints que consume la app:

| Método | Ruta | Uso |
|---|---|---|
| GET | `/productos` | Listado paginado del inventario |
| GET | `/productos/{id}` | Carga del producto en edición |
| POST | `/productos` | Registro de producto |
| PUT | `/productos/{id}` | Actualización de producto |
| DELETE | `/productos/{id}` | Baja de producto |
| GET | `/clientes`, `/clientes/{id}` | Cartera de clientes |
| POST/PUT/DELETE | `/clientes`, `/clientes/{id}` | Registro, actualización y baja |
| GET | `/categorias` | Catálogo para el selector del formulario |
| POST | `/ventas` | Venta (`{ clienteId, detalles: [{ productoId, cantidad }] }`) |

## Pruebas

- `commonTest` cubre modelos, validaciones, casos de uso (éxito y error), repositorios en memoria, fakes, los cuatro ViewModels, el filtro de los listados, el formato de precio, los mappers DTO ↔ dominio, las cuatro APIs con `MockEngine` y el grafo de Koin completo.
- Ejecución: `./gradlew :shared:testAndroidHostTest` (**115 casos, 0 fallos**).
- Las pruebas de red no salen a internet: `crearHttpClient(MockEngine { … })` reproduce las respuestas y comprueba rutas, verbos, query de paginación y cuerpo JSON.

## Cómo ejecutar

1. Abrir Android Studio → File > Open → carpeta `PharmaMobil`.
2. Sincronizar Gradle y correr la configuración `androidApp`.

### Compilar con Gradle

```bash
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
```

## Estructura del proyecto (Gradle)

```
PharmaMobil/
├── androidApp/                     ← Módulo Android (MainActivity, MainApplication)
├── shared/                         ← Módulo compartido KMP (commonMain / androidMain / iosMain)
├── iosApp/                         ← App iOS (Swift)
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/libs.versions.toml       ← Catálogo de dependencias
```

## Dependencias principales

| Dependencia | Versión | Uso |
|---|---|---|
| Kotlin | 2.4.10 | Lenguaje |
| Compose Multiplatform | 1.11.1 | UI declarativa |
| Material3 | 1.11.0-alpha07 | Componentes Material Design |
| Ktor Client | 3.5.2 | Cliente HTTP (core, content-negotiation, logging, kotlinx-json) |
| Ktor Mock | 3.5.2 | `MockEngine` en `commonTest` |
| Ktor OkHttp / Darwin | 3.5.2 | Motores HTTP de Android e iOS |
| Lifecycle ViewModel | 2.11.0-beta01 | ViewModel en commonMain |
| Koin | 4.2.2 | Inyección de dependencias |
| kotlinx-coroutines-test | 1.10.2 | Pruebas con MainDispatcher de prueba |
| AGP | 9.0.1 | Android Gradle Plugin |

---

Proyecto académico para la **Universidad Privada de los Andes - UPEU**.