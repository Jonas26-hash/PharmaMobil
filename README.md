# PharmaMobil

Aplicación multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** para la gestión de medicamentos, ventas y clientes de una cadena de boticas.

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin Multiplatform | Lógica compartida en `commonMain` (Android + iOS) |
| Compose Multiplatform | Interfaz de usuario declarativa |
| Coroutines | Operaciones `suspend` con latencia simulada |
| StateFlow | Estados reactivos que refrescan la UI automáticamente |
| Clean Architecture | Capas `domain → data → presentation` separadas y testables |
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
│   ├── model/                      ← Producto, Cliente, Venta, DetalleVenta, Pedido…
│   ├── repository/                 ← Contratos (ProductoRepository, ClienteRepository)
│   └── usecase/                    ← Validación y lógica de negocio + Result<T>
├── data/
│   └── repository/                 ← Implementaciones en memoria con semillas
└── presentation/
    ├── components/                 ← EstadoVacio, MensajeExito, ValidatedTextField
    ├── inicio/                     ← Pantalla de inicio con accesos rápidos
    ├── producto/                   ← Registro, inventario con filtros y venta
    └── cliente/                    ← Registro y cartera de clientes
```

Las dependencias apuntan hacia el centro: `presentation` usa casos de uso, `domain` define contratos y `data` los implementa. La UI nunca toca el repositorio directamente.

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
    val categoria: String = "",
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
    val direccion: String? = null
)
```

- Propiedad calculada `nombreCompleto`.
- Teléfono, correo y dirección opcionales: no pueden ser cadenas vacías.

### Venta y DetalleVenta

- `DetalleVenta` calcula su `subtotal = cantidad * precioUnitario`.
- `Venta` suma los subtotales en `total` y descuenta stock al registrarse.

## Capa de datos

- `ProductoRepositorioEnMemoria` y `ClienteRepositorioEnMemoria`: almacenamiento en memoria con **datos semilla** (7 productos y 3 clientes).
- El id lo asigna el repositorio para evitar identificadores duplicados.
- Un `Mutex` protege la lista ante llamadas concurrentes; los cambios se publican con `StateFlow`.
- `delay()` simula la latencia de un backend REST (se ve el estado de carga de la UI).
- Reglas de duplicados: nombre de producto (insensible a mayúsculas) y DNI de cliente.

## Casos de uso

- `RegistrarProductoUseCase` y `RegistrarClienteUseCase`: validan antes de persistir y devuelven errores por campo (`ErroresDeProducto` / `ErroresDeCliente`, una excepción tipada cada una).
- `ListarProductosUseCase` y `ListarClientesUseCase`: entregan el inventario / la cartera.
- `RegistrarVentaUseCase`: corta la venta de un producto al vender desde el inventario.
- Todo se envuelve en `Result<T>` mediante `resultadoDe`, que relanza `CancellationException` para no interrumpir el flujo de una corrutina cancelada.

## Pantallas

| Pantalla | Contenido |
|---|---|
| **Inicio** | Tarjeta de bienvenida y accesos rápidos a productos, clientes y pedidos |
| **Productos** | Formulario de registro en tarjeta + inventario con filtros **Activos / Inactivos / Bajo stock** + botón **Vender** con snackbar del total |
| **Clientes** | Formulario rico en tarjeta (nombre, apellido, DNI, teléfono, correo, dirección) + cartera |
| **Pedidos** | Módulo en construcción (estado vacío) |

- Navegación con **drawer único** en todas las resoluciones y barra superior verde.
- Botón **Modo oscuro** en el drawer.
- Estados tipados en cada pantalla: `Cargando`, `SinDatos`, `ConDatos`, `Error` (solo uno activo, el compilador obliga a resolverlos con `when`).
- Validación visible por campo, mensajes de éxito y estados vacíos con icono.

## Inyección de dependencias (Koin)

- `dataModule`: repositorios (singleton).
- `domainModule`: casos de uso (`factory`).
- `presentationModule`: ViewModels con `viewModel { }`.
- `initKoin { }` arranca Koin desde `MainApplication` (Android) y desde `iOSApp.swift` (iOS).

## Pruebas

- `commonTest` cubre modelos, casos de uso (éxito y error), repositorios en memoria, fakes, ViewModels, filtros de inventario y el grafo de Koin completo.
- Ejecución: `./gradlew :shared:testAndroidHostTest` (83 casos, 0 fallos).

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
| Lifecycle ViewModel | 2.11.0-beta01 | ViewModel en commonMain |
| Koin | 4.2.2 | Inyección de dependencias |
| kotlinx-coroutines-test | 1.10.2 | Pruebas con MainDispatcher de prueba |
| AGP | 9.0.1 | Android Gradle Plugin |

---

Proyecto académico para la **Universidad Privada de los Andes - UPEU**.