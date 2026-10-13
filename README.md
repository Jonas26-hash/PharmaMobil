# PharmaMobil

Aplicación multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** para la gestión de medicamentos, ventas y clientes de una cadena de boticas.

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin Multiplatform | Lógica compartida en `commonMain` (Android + iOS) |
| Compose Multiplatform | Interfaz de usuario declarativa |
| Ktor Client 3.5.2 | Cliente HTTP con Content Negotiation JSON, logging y timeouts |
| Ktor Auth (Bearer) | Autenticación por token: adjunta `Authorization` y detecta el 401 |
| Android Keystore / iOS Keychain | Almacenamiento cifrado del token de sesión |
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
│   ├── auth/                       ← Credenciales, Sesion (permisos) y AuthRepository
│   ├── security/                   ← Contrato AlmacenSeguro (guardar/leer token)
│   └── usecase/                    ← Validación y lógica de negocio + Result<T>
├── data/
│   ├── remote/                     ← Cliente HTTP de Ktor: DTO, mappers, APIs, repositorios remotos
│   ├── auth/                       ← GestorSesion (StateFlow<Sesion?>)
│   ├── security/                   ← AlmacenSeguroAndroid (Keystore) / AlmacenSeguroKeychain (iOS)
│   └── repository/                 ← Implementaciones en memoria (referencia para pruebas)
└── presentation/
    ├── components/                 ← EstadoVacio, MensajeExito, ValidatedTextField
    ├── login/                      ← LoginUiState, LoginViewModel y LoginScreen
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

## Autenticación y sesión (Sesión 10)

La app arranca en un **login** y solo muestra el contenido cuando hay una sesión válida. El token se guarda **cifrado** en el dispositivo y se adjunta automáticamente a cada petición protegida.

### Flujo

1. `LoginViewModel` recibe usuario y contraseña y llama a `AuthRepository.iniciarSesion(Credenciales)`.
2. `AuthRepositorioRemoto` hace `POST /auth/login`; si el 401 vuelve, se traduce a `CredencialesInvalidas` («Usuario o contraseña incorrectos»).
3. Con la respuesta se construye el `Sesion(token, usuario, rol, permisos)` y se guarda el **token** en `AlmacenSeguro` (Keystore en Android, Keychain en iOS).
4. `GestorSesion` expone `StateFlow<Sesion?>`; `App.kt` lo observa y conmuta entre `LoginScreen` y `ContenidoAutenticado`.
5. Al iniciar la app, `recuperarSesion()` lee el token del almacén y pide `GET /auth/me` para reconstruir la sesión (si el token caducó, vuelve al login).
6. **Cerrar sesión** limpia la sesión en memoria y borra el token del almacén.

### Cliente HTTP

`HttpClientFactory.crearHttpClient(motor, leerToken, alExpirarSesion)`:

- `install(Auth) { bearer { loadTokens { leerToken()?.let { BearerTokens(it, null) } }; refreshTokens { null } } }` adjunta el `Bearer` en cada petición.
- `Logging` **oculta** la cabecera `Authorization` para no filtrar el token en los logs.
- `HttpResponseValidator`: un **401** en una ruta que no sea `/auth/login` dispara `alExpirarSesion()` (la app vuelve al login); un **403** conserva la sesión y se muestra como «No tienes permisos para esta acción».

### Permisos en la interfaz

`Sesion.puede(permiso)` / `GestorSesion.puede(permiso)` filtran el menú y los botones:

- El drawer solo muestra los destinos para los que el rol tiene permiso.
- `ProductoScreen` y `ClienteScreen` reciben `puedeRegistrar`: si el rol no tiene `productos:registrar` / `clientes:registrar`, el botón de registro no aparece.

Los nombres de permiso (`productos:listar`, `productos:registrar`, `clientes:listar`, `clientes:registrar`, `pedidos:ver`) están centralizados en `domain/auth/Permisos.kt`.

> **Alcance y limitación conocida:** el backend **pharmaSoft** (hasta la fecha de esta entrega) **no expone endpoints de autenticación** (`/auth/login`, `/auth/me`) ni roles. Por eso la integración se verificó **de extremo a extremo contra un servidor mock local** y con pruebas de red (`MockEngine`); los casos que dependen del backend real (login real, matriz de roles, caducidad/refresco de token) quedan marcados **PENDIENTE** en la evidencia de la sesión. El contrato REST de auth sigue la forma estándar `{ token, usuario, rol, permisos }` para que, cuando el backend lo implemente, la app solo cambie la URL.

## Inyección de dependencias (Koin)

- `dataModule`: motor HTTP, `AlmacenSeguro`, `GestorSesion` y `HttpClient` como `single` (uno solo en toda la app), APIs, repositorios remotos y `AuthRepository`.
- `domainModule`: casos de uso (`factory`).
- `presentationModule`: los ViewModels (`viewModel { }`), incluido `LoginViewModel`.
- `initKoin { }` arranca Koin desde `MainApplication` (Android) y desde `iOSApp.swift` (iOS). El `AlmacenSeguro` se aporta desde el módulo de plataforma (`PlatformModule.android.kt` en Android, `KoinInit.kt` en iOS).

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

> **Dispositivo físico con `adb reverse` (sin tocar IP ni firewall).** Con el teléfono conectado por USB, `adb reverse tcp:8080 tcp:8080` hace que el `127.0.0.1:8080` del teléfono apunte al `127.0.0.1:8080` de la PC. Así basta compilar con la URL local:
>
> ```bat
> adb reverse tcp:8080 tcp:8080
> .\gradlew :androidApp:installDebug -PpharmaApiBaseUrl=http://127.0.0.1:8080/api/v1
> ```
>
> Es la forma recomendada con un teléfono real: evita depender de la IP de la red (que cambia) y de reglas del Firewall. Funciona igual con el backend real `pharmaSoft` o con el servidor mock de evidencia.

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
| POST | `/auth/login` | Autenticación: devuelve `{ token, usuario, rol, permisos }` (contrato previsto; backend **PENDIENTE**) |
| GET | `/auth/me` | Reconstruye la sesión desde el token (contrato previsto; backend **PENDIENTE**) |

## Pruebas

- `commonTest` cubre modelos, validaciones, casos de uso (éxito y error), repositorios en memoria, fakes, los ViewModels (incluido `LoginViewModel`), el filtro de los listados, el formato de precio, los mappers DTO ↔ dominio, las APIs con `MockEngine` (productos, clientes, categorías, ventas y **auth**) y el grafo de Koin completo.
- Ejecución: `./gradlew :shared:testAndroidHostTest` (**159 casos, 0 fallos**).
- Las pruebas de red no salen a internet: `crearHttpClient(MockEngine { … })` reproduce las respuestas y comprueba rutas, verbos, query de paginación y cuerpo JSON.
- `MensajesDeErrorTest`: cubre que 401 → `NoAutorizado`/`CredencialesInvalidas` y 403 → `Prohibido`, y que `esFalloDeAutenticacion` los reconoce.
- `SesionTest` fija la lógica de permisos (`puede`/`puedeAlguno`) y `AuthApiTest` / `AuthRepositorioRemotoTest` verifican el login y `/auth/me` contra `MockEngine`, incluido el mapeo del 401 a credenciales inválidas.
- `ListadoDeActivosTest` fija el comportamiento de la baja lógica: `DELETE` devuelve 204 pero el registro queda con `estado = false`, así que el repositorio filtra los desactivados al listar.

### Verificación contra el backend real

Comprobado con `pharmaSoft` (Oracle 23ai en Docker) y la app instalada en un dispositivo Android físico:

| Caso | Resultado |
|------|-----------|
| `GET /api/health` | 200 |
| `GET /api/v1/productos` | 200, devuelve solo los activos tras el filtro |
| `GET /api/v1/productos/{id}` | 200 con el objeto simple |
| `GET /api/v1/productos/99999` | 404, la app muestra «No se encontró el recurso» |
| `GET /api/v1/productos/abc` | 400 por parámetro inválido |
| `POST /api/v1/productos` | 201 con el id asignado por el servidor |
| `PUT /api/v1/productos/{id}` | 200 con los campos actualizados |
| `DELETE /api/v1/productos/{id}` | 204 y el registro queda con `estado = false` |
| Alta, edición y baja desde la app | El contador pasó de 2 a 1 cliente y la lista se actualizó |

> Nota sobre el contrato: `DELETE` es una **baja lógica**. El registro no desaparece de la base, y el backend ignora el parámetro `?estado=`, por eso el filtrado de los inactivos se hace en el repositorio del cliente.

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
| Ktor Client | 3.5.2 | Cliente HTTP (core, content-negotiation, logging, auth, kotlinx-json) |
| Ktor Mock | 3.5.2 | `MockEngine` en `commonTest` |
| Ktor OkHttp / Darwin | 3.5.2 | Motores HTTP de Android e iOS |
| Lifecycle ViewModel | 2.11.0-beta01 | ViewModel en commonMain |
| Koin | 4.2.2 | Inyección de dependencias |
| kotlinx-coroutines-test | 1.10.2 | Pruebas con MainDispatcher de prueba |
| AGP | 9.0.1 | Android Gradle Plugin |

---

Proyecto académico - UPEU.
