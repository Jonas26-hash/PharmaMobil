# RETO 02 – SESIÓN 4: Clasificación con Tabs, Material 3 centralizado y Diseño Adaptativo

**Proyecto:** PharmaMobil (Kotlin Multiplatform / Compose Multiplatform 1.11.1 · material3 1.11.0-alpha07)
**Dispositivo de prueba:** Celular físico Huawei NLA-LX3 (serial `A87MBB5913103112`) · pantalla 720×1600 · densidad física 320
**Fecha:** 1 de septiembre de 2026

---

## 1. Datos de la sesión

- **Actividad:** RETO 02 / SESIÓN 4 – Clasificación de inventario con pestañas, Mock Data centralizado, Material 3 corporativo, temas claro/oscuro, recurso compartido Compose y navegación adaptativa (compacto / mediana / amplia).
- **Modalidad:** El proyecto existente se **amplió** (no se reemplazó ningún funcionamiento que ya funcionaba). No se creó un proyecto nuevo.
- **Metodología de evidencia:** verificación por árbol de accesibilidad (`uiautomator dump`), capturas de pantalla `screencap` + `adb pull`, pruebas unitarias Gradle y compilación de release-verificación.

## 2. Descripción del reto

Partiendo de la app operativa de la Sesión 3 (lista de medicamentos, clientes, pedidos, formularios con validación y tema claro/oscuro de la Sesión 4), se solicitó:

1. Tabs **Activos / Inactivos / Bajo stock** exclusivamente en Productos.
2. Clasificación automática del inventario con una única regla de Bajo Stock.
3. Mock Data con un **conjunto mínimo obligatorio** de medicamentos.
4. Material 3 centralizado (colorScheme + typography + shapes).
5. Tema claro/oscuro verificado en todas las pantallas.
6. Recurso compartido Compose (logo) consumido en Inicio.
7. Navegación adaptativa según el ancho de la ventana.
8. Conservar validaciones, pruebas funcionales, tests, capturas e informe.

## 3. Objetivos

- Implementar la clasificación por pestañas sin romper el registro, la lista ni la venta.
- Centralizar la identidad visual en un único tema Material 3.
- Hacer la navegación adaptativa a teléfono, tablet mediana y pantalla amplia.
- Mantener los 29 tests existentes verdes y añadir pruebas de las nuevas reglas.
- Documentar decisiones técnicas con su **por qué** y dejar evidencia real (sin inventar).

## 4. Estado inicial (Fase 1) – antes de modificar código

| Ítem | Estado al iniciar |
|---|---|
| Compilación previa a cambios | `BUILD SUCCESSFUL` (antes de tocar código) |
| Navegación | `ModalNavigationDrawer` fijo (solo compacto) |
| Pantalla Productos | Lista única sin tabs · `Medicamento` sin campo `activo` |
| Mock data | 9 productos con valores aleatorios, sin el conjunto obligatorio |
| Tema | `PharmaMobilTheme` con paletas por defecto de Material 3 |
| Logo | Inicio sin elemento visual compartido |
| Validaciones | `ProductoValidator` (5 mensajes, Sesión 3) y `ClienteValidator` intactos |
| Tests previos | 27 (ClienteValidator 7 + ProductoValidator 12 + plantilla 2 …) — ver detalle en §16 |

La auditoría de este estado inicial determinó los huecos exactos que se implementarían (tabs, `activo`, mock data, tema corporativo, logo, adaptativo).

## 5. Cambios realizados (Fases 2–4)

| Archivo | Cambio |
|---|---|
| `Medicamento.kt` | Nuevo campo `activo: Boolean = true` (default: no rompe constructores existentes) |
| `ProductoRepositoryImpl.kt` | `generarMedicamentosIniciales()` con el conjunto mínimo obligatorio; `registrarMedicamento` crea el producto con `activo = true` |
| `TabInventario.kt` (**nuevo**) | Enum `ACTIVOS / INACTIVOS / BAJO_STOCK` con `titulo`, `filtrar()` y `esBajoStock()` |
| `FarmaciaScreen.kt` | `PrimaryScrollableTabRow` + `HorizontalPager` con estado `tabSeleccionada` sincronizado |
| `PharmaMobilTheme.kt` | ColorScheme corporativo claro/oscuro + `Typography()` + `Shapes()` explícitos |
| `pharmamobil_logo.xml` (**nuevo**) | Vector en `commonMain/composeResources/drawable/` |
| `InicioScreen.kt` | Consume el logo con `painterResource(Res.drawable.pharmamobil_logo)` |
| `App.kt` | `BoxWithConstraints` + 3 variantes de navegación + componentes `MenuNavegacion`, `RailNavegacion`, `EstructuraPrincipal`, `DrawerHeader`, `Destino` |
| `TabInventarioTest.kt` (**nuevo**) | 8 tests de las reglas de clasificación |

## 6. FASE 5 – Tabs en Productos (implementación)

- **Estado:** `var tabSeleccionada by remember { mutableIntStateOf(0) }` (equivalente exacto al `mutableStateOf(0)` solicitado).
- **Componentes Material 3:** `PrimaryScrollableTabRow` (reemplazo no-obsoleto de `ScrollableTabRow` en material3 1.11.0-alpha07, ver incidencia 6) + `HorizontalPager` de `foundation.pager`.
- **Sincronización:** al tocar una Tab se fija `tabSeleccionada` y se anima el pager; el `LaunchedEffect(pagerState.currentPage)` reconoce el gesto de deslizamiento y actualiza la pestaña seleccionada. Doble vía correcta.
- **Alcance:** únicamente en la pantalla Productos. Inicio/Clientes/Pedidos siguen siendo destinos de navegación, nunca pestañas.
- **Comportamiento por pestaña:**
  - `Activos`: solo `producto.activo == true`.
  - `Inactivos`: solo `!producto.activo` (tarjeta con etiqueta **Inactivo** y botón **Sin stock**).
  - `Bajo stock`: `esBajoStock(producto)` → subconjunto de Activos.
  - Empty states por pestaña (`No hay productos en "…"`).
- **Registro → actualización:** el repositorio es reactivo (`MutableStateFlow` observado por el ViewModel); al registrar, el producto aparece al instante en la pestaña que le corresponde **sin recargar**.

## 7. Regla de Bajo Stock – decisión documentada

```kotlin
const val LIMITE_BAJO_STOCK = 5
fun esBajoStock(producto: Medicamento): Boolean =
    producto.activo && producto.stock <= LIMITE_BAJO_STOCK
```

**Por qué se eligió así:**
- `stock <= 5` cumple el valor propuesto por la guía de forma literal.
- Se añade `activo &&` para que **Bajo stock sea subconjunto de Activos**: los productos inactivos **nunca** aparecen como de bajo stock, incluso si su stock es 0. Ejemplo de referencia: `Loratadina` (stock 0, inactiva) solo figura en **Inactivos**, replicando lo que la propia guía muestra en su ejemplo de "Inactivos".
- Caso límite documentado: un producto **activo con stock 0 sí aparece en Bajo stock** (generalización del stock 0 permitido que ya soportaba la Sesión 3).
- Verificado por tests (TabInventarioTest, §16) y por el dispositivo (PRUEBA 2 y PRUEBA 6).

## 8. FASE 3 – Mock Data con el conjunto mínimo obligatorio

| Nombre | Precio | Stock | activo | Categoría | Requiere receta |
|---|---|---|---|---|---|
| Paracetamol | 15.50 | 100 | ✅ | Analgésico | ❌ |
| Ibuprofeno | 18.90 | 50 | ✅ | Antiinflamatorio | ❌ |
| Amoxicilina | 25.00 | 5 | ✅ | Antibiótico | ✅ |
| Loratadina | 12.50 | 0 | ❌ (inactivo) | Antihistamínico | ❌ |
| Diclofenaco | 20.00 | 3 | ✅ | Antiinflamatorio | ❌ |
| Omeprazol | 5.75 | 18 | ✅ | Gastrointestinal | ❌ |
| Vitamina C | 6.00 | 30 | ✅ | Suplemento | ❌ |

Los 5 casos mínimos exigidos por la guía están presentes (Paracetamol, Ibuprofeno, Amoxicilina, Loratadina inactiva, Diclofenaco). Se conservaron Omeprazol y Vitamina C como datos complementarios.

## 9. Material 3 centralizado

- `PharmaMobilTheme` define y aplica en todos los componentes:
  - `LightColors` / `DarkColors` con paleta corporativa (primario `#00606B` teal, primario oscuro `#8ECFD8`, tarjeta/superficie neutras). El color base actualiza automáticamente los `error`, `primaryContainer`, etc., en ambos modos.
  - `PharmaTypography = Typography()` y `PharmaShapes = Shapes()` explícitos, pasados por `MaterialTheme(...)`.
- Los componentes consumen **roles** del tema (`colorScheme.primary`, `onSurfaceVariant`, `error`, `typography.*`), sin colores `Color(...)` hardcodeados dentro de las pantallas.

## 10. Temas claro / oscuro

- El switch `Modo oscuro` (presente en el drawer, en el rail y en el drawer permanente) conmuta `darkTheme` al instante, sin recrear la actividad ni perder la pantalla seleccionada.
- Verificado en el dispositivo: PRUEBA 3 (claro) y PRUEBA 4 (oscuro) sobre la misma pantalla Productos, además de recorrido previo por Inicio/Clientes/Pedidos capturado en la Sesión 4. Capturas `08_light_theme.png` y `09_dark_theme.png`.
- El logo vectorial es el mismo en ambos temas (dibujo a trazo sobre fondo transparente), no es una mera inversión de colores.

## 11. Recurso compartido Compose (logo)

- Ubicación: `shared/src/commonMain/composeResources/drawable/pharmamobil_logo.xml` (vector; **sin** gradientes ni `aapt` de Android, para máxima compatibilidad multiplataforma).
- Paquete generado: `pharmamobil.shared.generated.resources` → imports:
  ```kotlin
  import pharmamobil.shared.generated.resources.Res
  import pharmamobil.shared.generated.resources.pharmamobil_logo
  import org.jetbrains.compose.resources.painterResource
  ```
- Consumido en `InicioScreen` (`Image` de 140.dp, `contentDescription = "Logo PharmaMobil"`).
- Evidencia en dispositivo: nodo de accesibilidad `content-desc="Logo PharmaMobil"` presente en los dumps de PRUEBA 1 y en la captura `10_recurso_logo.png`.

## 12. Diseño adaptativo (compacto / mediana / amplia)

En `App.kt`, `BoxWithConstraints` + `TipoNavegacion`:

| Variante | Ancho | Navegación | Evidencia |
|---|---|---|---|
| **COMPACTA** | < 600.dp | `ModalNavigationDrawer` con hamburguesa (`content-desc="Abrir menú"`) | `11_compact_phone.png` |
| **MEDIANA** | 600 … < 840.dp | `NavigationRail` (rail fijo a la izquierda, sin hamburguesa) | `12_medium_tablet.png` |
| **AMPLIA** | ≥ 840.dp | `PermanentNavigationDrawer` (drawer siempre visible) | `13_expanded.png` |

- Mismos 4 destinos (`Inicio`, `Productos`, `Clientes`, `Pedidos`) y el **mismo estado** `pantallaActual` en las tres variantes; el switch de tema se mantiene en las tres.
- **Método de verificación en dispositivo físico** (sin necesidad de emulador): se varió la densidad con `adb shell wm density` sobre el mismo Huawei: a 160 → 720 px / 1,0 = **720 dp (mediana)**, y a 120 → 720 px / 0,75 = **960 dp (amplia)**; luego `wm density reset`. En cada configuración el árbol de accesibilidad confirmó el componente esperado:
  - Mediana: los 4 ítems del rail presentes, **sin** "Abrir menú" ni "Modo oscuro" (el switch del rail no lleva texto).
  - Amplia: los 4 ítems **siempre visibles** junto a "Modo oscuro", sin hamburguesa.
- Tras el reset se verificó que la app vuelve al modo **compacto** normal (hamburguesa presente, 360 dp reales).

## 13. Responsabilidad de componentes y textos conservados

- `App.kt` solo decide el contenedor de navegación; **no duplica pantallas**: `EstructuraPrincipal` es la misma en las tres variantes y el `when (pantallaActual)` sigue reutilizando `InicioScreen`, `FarmaciaScreen`, `RegistroMedicamentoScreen`, `ClienteScreen`, `RegistroClienteScreen` y `PedidosScreen`.
- `FarmaciaScreen` delega la clasificación en `TabInventario` (reglas de dominio fuera de la UI).
- Se conservaron en su lugar la barra superior con "Clientes" / "+ Registrar", los textos de validación de la Sesión 3 y los mensajes de error del formulario.

## 14. Matriz de decisiones (con POR QUÉ)

| Decisión | Opción elegida | ¿Por qué? |
|---|---|---|
| Regla de bajo stock | `activo && stock <= 5` | Cumple el límite literal de la guía y evita que inactivos (p. ej. Loratadina stock 0) contaminen la pestaña Bajo stock; Bajo stock queda definido como subconjunto de Activos |
| Componente de Tabs | `PrimaryScrollableTabRow` | `ScrollableTabRow` está **obsoleta** en material3 1.11.0-alpha07; su reemplazo oficial mantiene la misma API y es Material 3 |
| Estado de la pestaña | `mutableIntStateOf(0)` + pager sincronizado | Doble sincronización (tab→pager y gesto→tab) para no desincronizar al deslizar |
| `activo` en el modelo | `Boolean = true` con default | No obliga a cambiar constructor en tests ni en código existente |
| Registro de PRUEBA 5/6 | Productos nuevos con los mismos valores límite + verificación del seed | Paracetamol/Amoxicilina **ya existen** en el mock: el repositorio rechaza duplicados por nombre, por lo que re-registrarlos literalmente sería un error de duplicado (ver incidencia 4) |
| Prueba adaptativa | `wm density` sobre el físico | Sin emulador levantado, se cubren mediana (720 dp) y amplia (960 dp) con evidencia real; se resetea al final |
| Almacenamiento de capturas | `documentacion/reto02_sesion4/capturas/` | Entregable del reto dentro del repositorio; nombres fijos `01…13` más complementarias |

## 15. Matriz de incidencias (reales, sin inventar)

| # | Incidencia | Causa raíz | Solución aplicada |
|---|---|---|---|
| 1 | El Snackbar "Medicamento registrado: …" no aparece en los dumps de uiautomator | Los Snackbars de Compose pueden no exponerse como nodos del árbol de accesibilidad en este equipo/Huawei | Verificación funcional por **limpieza del formulario + aparición del producto en la lista** (dumps) y captura de pantalla en el instante del mensaje |
| 2 | El tap sobre "Registrar Medicamento" a veces no enviaba el formulario | El árbol contiene dos nodos con ese texto (título del `TopAppBar` + botón); el tap podía caer sobre el título | Seleccionar el **último nodo en pantalla** (mayor `y2`) = botón real; el título queda descartado |
| 3 | `input keyevent 4` sacaba de la app | La Activity no declara `BackHandler`; el back del sistema finaliza la actividad | Navegación de retorno con la flecha `content-desc="Volver"` del formulario (no con back del sistema) |
| 4 | No se puede registrar "Paracetamol 15.50/100" ni "Amoxicilina 25.00/5" por duplicado | Ambos ya existen en el mock data obligatorio y el repo lanza "Ya existe un medicamento con el nombre …" | PRUEBA 5/6 se prueban con productos nuevos de los mismos valores de borde y **verificando** los del seed (Amoxicilina/Diclofenaco ya clasificados) |
| 5 | Primeras capturas no generaban archivos | `adb pull` falló en silencio porque el directorio destino no existía | Crear `capturas/` y re-tomar el set completo programáticamente |
| 6 | Warning de deprecación al compilar | `ScrollableTabRow` obsoleta | Migración a `PrimaryScrollableTabRow`; build limpio sin warnings |
| 7 | Tras registrar un producto con stock 5 no aparecía en Bajo stock en el primer dump | No era un bug: la tarjeta quedaba **debajo del viewport** (lista de 3 ítems) | Scroll + re-dump confirmaron la clasificación automática (`Ranitidina`/`Ambroxol Plus` en Bajo stock) |

## 16. Pruebas funcionales en dispositivo (resultado: 6/6)

| PRUEBA | Acción | Evidencia obtenida | Resultado |
|---|---|---|---|
| 1. Navegación | Inicio → (menú) → Productos → Clientes → Pedidos → Inicio | `PharmaMobil`+logo+tagline; menú `Inicio|Productos|Clientes|Pedidos|Modo oscuro`; `Medicamentos disponibles`; `Clientes registrados`; `Pantalla de pedidos`; Inicio de nuevo | ✅ |
| 2. Tabs | Activos / Inactivos / Bajo stock | Activos: Paracetamol/15.50/100, Ibuprofeno; Inactivos: Loratadina con "Inactivo" y "Sin stock", sin Paracetamol; Bajo stock: Amoxicilina y Diclofenaco, **sin Loratadina** | ✅ |
| 3. Tema claro | Productos en claro | Paracetamol + precio 15.50 visibles; captura 08 | ✅ |
| 4. Tema oscuro | Switch → Productos en oscuro | switch `checked=true`, Paracetamol visible, app al frente; captura 09; vuelta a claro | ✅ |
| 5. Registro (éxito + actualización) | Alta de producto nuevo (Clorfenamina 9.50/120; Gel Humectante 21.00/80) | Snackbar de éxito (captura 06) + **aparición inmediata en Activos** sin recargar (captura 06b) | ✅ |
| 6. Clasificación automática | Verificar seed + alta de nuevo producto stock 5 (Ranitidina 12.00/5; Ambroxol Plus 14.50/5) | Amoxicilina (5) y Diclofenaco (3) ya en Bajo stock; el nuevo con stock 5 **aparece solo en Bajo stock** (capturas 07 y 07c) | ✅ |

## 17. Pruebas automatizadas

Suite completa (`:shared:testAndroidHostTest`): **29 tests, 0 fallos, 0 errores**.

| Suite | Tests | Resultado |
|---|---|---|
| ClienteValidatorTest | 7 | ✅ |
| ProductoValidatorTest | 12 | ✅ |
| TabInventarioTest (**nuevos**) | 8 | ✅ |
| SharedCommonTest (plantilla) | 1 | ✅ |
| SharedLogicAndroidHostTest (plantilla) | 1 | ✅ |

Casos cubiertos por `TabInventarioTest`: activos incluyen a los activos; inactivos incluyen solo a los inactivos (Loratadina stock 0); bajo stock **incluye** un activo con stock == 5 y stock == 3; **excluye** inactivos con stock 0; **incluye** activo con stock 0; Bajo stock ⊆ Activos; límite == 5; filtrado por subtítulos de Activos.

**Compilación:** `:androidApp:compileDebugKotlin` → `BUILD SUCCESSFUL` con el proyecto final (sin warnings de deprecación).

## 18. Checklist del reto (8/8)

| # | Criterio | Cumple |
|---|---|---|
| 1 | Tabs Activos / Inactivos / Bajo stock en Productos, con `tabSeleccionada` | ✅ |
| 2 | Regla de Bajo Stock única y **documentada** (`activo && stock <= 5`) | ✅ |
| 3 | Mock Data con el conjunto mínimo obligatorio (los 5 casos) | ✅ |
| 4 | Material 3 centralizado (colorScheme + typography + shapes) | ✅ |
| 5 | Tema claro/oscuro verificado sin romper pantallas | ✅ |
| 6 | Recurso compartido `pharmamobil_logo` consumido en Inicio | ✅ |
| 7 | Diseño adaptativo compacto / mediano / amplio con los mismos 4 destinos | ✅ |
| 8 | Funcionalidad previa conservada (validaciones, registro, venta, tests) | ✅ |

**Resultado: 8/8 CUMPLE.**

## 19. Capturas de evidencia

Directorio: `documentacion/reto02_sesion4/capturas/`

| Archivo | Contenido |
|---|---|
| `01_inicio.png` | Inicio con logo, título y tagline |
| `02_drawer.png` | Menú (drawer) con las 4 opciones y switch |
| `03_productos_activos.png` | Tab Activos (Paracetamol, Ibuprofeno…) |
| `04_productos_inactivos.png` | Tab Inactivos (Loratadina, etiqueta "Inactivo", "Sin stock") |
| `05_productos_bajo_stock.png` | Tab Bajo stock (Amoxicilina, Diclofenaco) |
| `06_registro_exitoso.png` | Mensaje de éxito al registrar (formulario tras el alta) |
| `06b_lista_actualizada.png` | Actualización inmediata de la lista Activos (*complementaria*) |
| `07_bajo_stock_amoxicilina.png` | Bajo stock en el tope (Amoxicilina visible) |
| `07c_bajo_stock_auto.png` | Clasificación automática del producto recién registrado (*complementaria*) |
| `08_light_theme.png` | Pantalla Productos en modo claro |
| `09_dark_theme.png` | Pantalla Productos en modo oscuro |
| `10_recurso_logo.png` | Inicio mostrando el recurso compartido |
| `11_compact_phone.png` | Variante compacta (teléfono) |
| `12_medium_tablet.png` | Variante mediana (NavigationRail) |
| `13_expanded.png` | Variante amplia (drawer permanente) |

Todas las capturas fueron tomadas con `adb screencap` sobre el dispositivo físico. Los estados de cada una están además respaldados por los dumps de accesibilidad citados en §16.

## Preguntas de reflexión

1. **¿Cuándo conviene un `NavigationRail` frente a un drawer?** El rail maximiza la visibilidad del destino en pantallas medianas (600–840 dp) sin quitar demasiado ancho al contenido; el drawer modal oculta todo y es ideal para compacto, donde el ancho es escaso.
2. **¿Por qué Bajo stock excluye a los inactivos?** Para que las pestañas sean una partición coherente del inventario: Activos ∪ Inactivos = todo, y Bajo stock es un subconjunto de Activos. De lo contrario, Loratadina (inactiva, stock 0) aparecería dos veces.
3. **¿Por qué el pager y la tab se sincronizan en dos direcciones?** Porque las Tabs marcan la fuente de verdad del estado y el deslizamiento horizontal del pager debe reflejar la tab activa; si solo se actualizara una vía, el gesto dejaría la pestaña desmarcada.
4. **¿Qué garantiza el `rememberPagerState(pageCount = { TabInventario.entries.size })`?** Que el número de páginas derive del enum, de modo que añadir una pestaña no requiera tocar el layout del pager.

## Conclusiones

- El reto se implementó **sobre el funcionamiento existente**, cumpliendo los 8 criterios del checklist sin duplicar pantallas ni romper validaciones.
- La regla de Bajo Stock quedó **explícita y testeada**, incluidos los casos límite (stock 0 activo vs inactivo, límite == 5).
- El tema Material 3 quedó centralizado en un solo archivo y se verificó en claro y oscuro en el dispositivo.
- La navegación adaptativa se demostró en el físico con tres anchos lógicos (360 dp compacto, 720 dp mediano, 960 dp amplio) con evidencia de accesibilidad.
- 29 tests verdes y compilación sin warnings respaldan la entrega; las 13 capturas exigidas (más 2 complementarias) documentan cada requisito.
- Se documentaron 7 incidencias reales con su causa raíz y solución; ninguna quedó oculta.

## Evidencias

- Código: cambios en `shared/` (modelo, repositorio, `TabInventario`, `FarmaciaScreen`, tema, `InicioScreen`, `App.kt`, recurso) y tests `TabInventarioTest.kt`.
- Resultados de consola: `BUILD SUCCESSFUL` + resumen de 29 tests (5 suites, 0 fallos).
- Dispositivo: dumps de accesibilidad de las 6 pruebas funcionales + capturas en `documentacion/reto02_sesion4/capturas/`.