# Farmacia KMP

Aplicación multiplataforma desarrollada con **Kotlin Multiplatform (KMP)** y **Compose Multiplatform** para la gestión de medicamentos y clientes de una farmacia.

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Kotlin Multiplatform | Lógica compartida en `commonMain` |
| Compose Multiplatform | Interfaz de usuario declarativa |
| Coroutines | Operaciones suspend (simulación de llamadas remotas) |
| Kotlin Flow / StateFlow | Estados reactivos y actualización automática de la UI |
| Data Classes | Modelado de entidades con `copy()`, `equals()`, `hashCode()` |
| Sealed Interface | Estados tipados: `Loading`, `Success`, `Error` |
| ViewModel | Manejo de estado de la pantalla sobreviviente a configuración |
| Repository Pattern | Separación entre fuente de datos y lógica de negocio |
| Null Safety | Uso de tipos nullable (`String?`, `?.`, `== true`) |

## Arquitectura por dominios

El proyecto está organizado en dos dominios independientes, cada uno con su propia capa `domain → data → presentation`:

```
shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/
├── App.kt                          ← Punto de entrada Compose
├── Util.kt                         ← Funciones compartidas
├── presentation/
│   └── model/
│       └── UiState.kt              ← Sealed interface compartido
├── producto/                       ← Dominio: Medicamentos y Ventas
│   ├── domain/
│   │   ├── model/
│   │   │   ├── Medicamento.kt
│   │   │   ├── DetalleVenta.kt
│   │   │   └── Venta.kt
│   │   └── repository/
│   │       └── ProductoRepository.kt
│   ├── data/
│   │   └── repository/
│   │       └── ProductoRepositoryImpl.kt
│   └── presentation/
│       ├── viewmodel/
│       │   └── ProductoViewModel.kt
│       └── ui/
│           ├── FarmaciaScreen.kt
│           └── RegistroMedicamentoScreen.kt
└── cliente/                        ← Dominio: Clientes
    ├── domain/
    │   ├── model/
    │   │   └── Cliente.kt
    │   └── repository/
    │       └── ClienteRepository.kt
    ├── data/
    │   └── repository/
    │       └── ClienteRepositoryImpl.kt
    └── presentation/
        ├── viewmodel/
        │   └── ClienteViewModel.kt
        └── ui/
            ├── ClienteScreen.kt
            └── RegistroClienteScreen.kt
```

## Modelo de dominio

### Medicamento

```kotlin
data class Medicamento(
    val id: String,
    val nombre: String,
    val descripcion: String?,        // nullable
    val precio: Double,
    val stock: Int,
    val requiereReceta: Boolean,
    val categoria: String
)
```

Validaciones en el bloque `init`:
- `nombre` no puede estar vacío
- `precio` no puede ser negativo
- `stock` no puede ser negativo

### DetalleVenta

```kotlin
data class DetalleVenta(
    val medicamentoId: String,
    val nombreMedicamento: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val subtotal: Double             // cantidad * precioUnitario
)
```

### Venta

```kotlin
data class Venta(
    val id: String,
    val fecha: String,
    val items: List<DetalleVenta>,
    val total: Double                // suma de subtotales
)
```

### Cliente

```kotlin
data class Cliente(
    val id: String,
    val nombre: String,
    val apellido: String,
    val dni: String,                 // exactamente 8 dígitos
    val telefono: String?,           // nullable
    val email: String?,              // nullable
    val direccion: String?           // nullable
)
```

Propiedad calculada: `nombreCompleto` → `"${nombre} ${apellido}"`

## Sealed Interface - UiState

```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}
```

Garantiza que solo existan 3 estados. El compilador obliga a manejar todos los casos con `when`.

## Repository Pattern

### ProductoRepository (interfaz)

| Método | Tipo | Descripción |
|---|---|---|
| `obtenerMedicamentos()` | `suspend` | Retorna lista de medicamentos con `delay(1500ms)` |
| `buscarMedicamento(id)` | `suspend` | Busca por ID con `delay(500ms)` |
| `registrarVenta(medicamentoId, cantidad)` | `suspend` | Valida stock, usa `copy()` para actualizar, `delay(1000ms)` |
| `registrarMedicamento(...)` | `suspend` | Valida duplicados, agrega a la lista, `delay(1000ms)` |
| `observarMedicamentos()` | `Flow` | Expone `StateFlow` que se actualiza automáticamente |

### ClienteRepository (interfaz)

| Método | Tipo | Descripción |
|---|---|---|
| `obtenerClientes()` | `suspend` | Retorna lista de clientes con `delay(1000ms)` |
| `registrarCliente(...)` | `suspend` | Valida DNI duplicado, `delay(1000ms)` |
| `observarClientes()` | `Flow` | Expone `StateFlow` que se actualiza automáticamente |

## Coroutines y Flow

### Simulación de llamadas remotas

```kotlin
override suspend fun obtenerMedicamentos(): List<Medicamento> {
    delay(1500)  // Simula 1.5 segundos de latencia de red
    return _medicamentos.value
}
```

### Actualización reactiva con MutableStateFlow

```kotlin
private val _medicamentos = MutableStateFlow(generarMedicamentosIniciales())

// Al vender: se crea copia inmutable con copy()
val medicamentoActualizado = medicamento.copy(
    stock = medicamento.stock - cantidad
)

// Se actualiza el Flow
_medicamentos.update { lista ->
    lista.map { if (it.id == medicamentoId) medicamentoActualizado else it }
}
```

### Observación en el ViewModel

```kotlin
repository.observarMedicamentos()
    .onEach { lista ->
        if (lista.isNotEmpty()) {
            _medicamentosState.value = UiState.Success(lista)
        }
    }
    .launchIn(viewModelScope)
```

La UI se refresca automáticamente cada vez que el repository emite una nueva lista.

## Manejo de errores

| Error | Dónde se valida | Mensaje |
|---|---|---|
| Medicamento inexistente | `ProductoRepositoryImpl` | "Medicamento no encontrado con id: ..." |
| Stock insuficiente | `ProductoRepositoryImpl` | "Stock insuficiente para ... Stock disponible: ..." |
| Cantidad inválida | `ProductoRepositoryImpl` | "La cantidad debe ser mayor a cero" |
| Nombre vacío | `Medicamento.init` | "El nombre del medicamento no puede estar vacío" |
| Precio negativo | `Medicamento.init` | "El precio no puede ser negativo" |
| Stock negativo | `Medicamento.init` | "El stock no puede ser negativo" |
| Nombre duplicado | `ProductoRepositoryImpl` | "Ya existe un medicamento con el nombre: ..." |
| DNI inválido | `Cliente.init` | "El DNI debe tener 8 dígitos" |
| DNI duplicado | `ClienteRepositoryImpl` | "Ya existe un cliente con el DNI: ..." |

Todos los errores se muestran mediante `UiState.Error(message)`.

## Pantallas de la aplicación

### 1. Lista de Medicamentos (`FarmaciaScreen`)

- TopAppBar con "FARMACIA KMP"
- LazyColumn con Cards de medicamentos
- Cada Card muestra: nombre, precio, stock, categoría, descripción
- Botón "Vender" (deshabilitado si stock = 0)
- Botón "+ Registrar" para agregar medicamento
- Botón "Clientes" para navegar a la lista de clientes
- Snackbar para notificar ventas exitosas o errores
- CircularProgressIndicator durante la carga

### 2. Registro de Medicamento (`RegistroMedicamentoScreen`)

- Formulario con 6 campos: nombre, descripción, precio, stock, categoría, requiere receta
- Validaciones en tiempo real (cada campo limpia su error al escribir)
- Botón "Registrar Medicamento" con CircularProgressIndicator
- Snackbar de confirmación al registrar exitosamente

### 3. Lista de Clientes (`ClienteScreen`)

- TopAppBar con "CLIENTES"
- LazyColumn con Cards de clientes
- Cada Card muestra: nombre completo, DNI, teléfono, email, dirección
- Campos opcionales se muestran solo si tienen valor (null safety)
- Botón "+ Registrar" y botón "Medicamentos" para navegar

### 4. Registro de Cliente (`RegistroClienteScreen`)

- Formulario con 6 campos: nombre, apellido, DNI, teléfono, email, dirección
- Validaciones: nombre, apellido y DNI son obligatorios; DNI debe tener 8 dígitos
- Teléfono, email y dirección son opcionales (nullable)
- Snackbar de confirmación al registrar exitosamente

## Navegación

```
FARMACIA KMP (lista medicamentos)
├── [Clientes] → CLIENTES (lista clientes)
│   ├── [+ Registrar] → REGISTRO_CLIENTE (formulario)
│   │   └── [Volver] → CLIENTES
│   └── [Medicamentos] → LISTA
└── [+ Registrar] → REGISTRO (formulario medicamento)
    └── [Volver] → LISTA
```

La navegación se implementa con un `enum class Pantalla` y `mutableStateOf` en `App.kt`.

## Datos iniciales

### Medicamentos (6)

| Nombre | Precio | Stock | Receta | Categoría |
|---|---|---|---|---|
| Paracetamol | $2.50 | 20 | No | Analgésico |
| Ibuprofeno | $3.25 | 15 | No | Antiinflamatorio |
| Amoxicilina | $8.50 | 10 | Sí | Antibiótico |
| Loratadina | $4.00 | 25 | No | Antihistamínico |
| Omeprazol | $5.75 | 18 | No | Gastrointestinal |
| Vitamina C | $6.00 | 30 | No | Suplemento |

### Clientes (3)

| Nombre | DNI | Teléfono | Email |
|---|---|---|---|
| María García López | 12345678 | 987654321 | maria.garcia@email.com |
| Juan Pérez Martínez | 87654321 | 912345678 | juan.perez@email.com |
| Ana Rodríguez Sánchez | 11223344 | null | ana.rodriguez@email.com |

## Cómo ejecutar

1. Abrir Android Studio
2. File > Open > seleccionar la carpeta `PharmaMobil`
3. Esperar a que Gradle sincronice
4. Seleccionar el run configuration `androidApp`
5. Click en Run (o `Shift+F10`)

### Compilar con Gradle

```bash
./gradlew :androidApp:assembleDebug
```

## Conceptos académicos demostrados

### Reto 01: Modelado de Dominio

- **Data Classes**: `Medicamento`, `DetalleVenta`, `Venta`, `Cliente` como data class
- **Inmutabilidad**: Uso de `copy()` para actualizar stock sin modificar el objeto original
- **Null Safety**: `descripcion: String?`, `telefono: String?`, operador `?.`, `?.isNotBlank() == true`
- **Validaciones**: Bloque `init` con `require()` en cada entidad
- **Sealed Interface**: `UiState<T>` con `Loading`, `Success`, `Error`

### Reto 02: Coroutines & Flow

- **Coroutines**: Funciones `suspend` con `delay()` para simular latencia de red
- **Flow**: `MutableStateFlow` que emite la lista de medicamentos/clientes
- **StateFlow**: Exposición de estados reactivos al ViewModel
- **viewModelScope**: Ejecución de coroutines ligadas al ciclo de vida del ViewModel
- **Estados Loading/Success/Error**: Flujo completo manejado con `when`

## Estructura del proyecto (Gradle)

```
PharmaMobil/
├── androidApp/                    ← Módulo Android
│   └── src/main/kotlin/
│       └── MainActivity.kt
├── shared/                        ← Módulo compartido KMP
│   └── src/
│       ├── commonMain/            ← Lógica compartida (Android + iOS)
│       ├── androidMain/           ← Código específico de Android
│       └── iosMain/               ← Código específico de iOS
├── iosApp/                        ← App iOS (Swift)
├── build.gradle.kts               ← Configuración raíz
├── settings.gradle.kts            ← Módulos incluidos
└── gradle/libs.versions.toml      ← Catálogo de dependencias
```

## Dependencias principales

| Dependencia | Versión | Uso |
|---|---|---|
| Kotlin | 2.4.10 | Lenguaje |
| Compose Multiplatform | 1.11.1 | UI declarativa |
| Material3 | 1.11.0-alpha07 | Componentes Material Design |
| Lifecycle ViewModel | 2.11.0-beta01 | ViewModel en commonMain |
| AGP | 9.0.1 | Android Gradle Plugin |

---

Proyecto académico - UPEU.
