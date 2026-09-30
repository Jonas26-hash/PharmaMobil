# INFORME FINAL – SESIÓN 4: Navegación, Material 3 y Tema

**Proyecto:** PharmaMobil (Kotlin Multiplatform / Compose Multiplatform)
**Dispositivo de prueba:** Celular físico Huawei NLA-LX3 (serial `A87MBB5913103112`) · pantalla 720×1600
**Fecha:** 1 de septiembre de 2026

---

## A. Estado inicial del proyecto

La Sesión 4 ya estaba **implementada al 100% en código** (trabajo de sesiones previas): navegación con `ModalNavigationDrawer`, `Scaffold` y `TopAppBar`; tema claro/oscuro; reutilización de las pantallas de la Sesión 3. Por lo tanto, el trabajo de esta sesión consistió en **verificar, probar en el dispositivo físico y documentar**, sin necesidad de modificar código funcional.

## B. Tabla de auditoría de archivos

| Archivo | Ruta | Acción | Estado |
|---|---|---|---|
| `App.kt` | `shared/src/commonMain/kotlin/pe/edu/upeu/pharmamobil/` | Auditoría: Scaffold único, drawer, TopAppBar, `pantallaActual` + `when` | CUMPLE |
| `Screen.kt` | `.../navigation/` | Sealed class de destinos, destino inicial `Inicio` | CUMPLE |
| `PharmaMobilTheme.kt` | `.../theme/` | `Theme` claro/oscuro (sin `dynamicColor`, con `colorScheme` Light/Dark) | CUMPLE |
| `InicioScreen.kt` | `.../presentation/inicio/` | Pantalla demo | CUMPLE |
| `PedidosScreen.kt` | `.../presentation/pedidos/` | Pantalla "en construcción" | CUMPLE |
| `FarmaciaScreen.kt` | `.../producto/presentation/ui/` | Pantalla Productos **reutilizada (Sesión 3)** | CUMPLE |
| `RegistroMedicamentoScreen.kt` | `.../producto/presentation/ui/` | Formulario **reutilizado (Sesión 3)** | CUMPLE |
| `ProductoValidator.kt` | `.../producto/presentation/` | Validaciones de Sesión 3 **intactas** | CUMPLE |
| `ClienteScreen.kt` | `.../cliente/presentation/ui/` | Pantalla Clientes (más completa que la demo) | CUMPLE |
| `RegistroClienteScreen.kt` | `.../cliente/presentation/ui/` | Formulario de cliente (content-only) | CUMPLE |
| `ClienteValidator.kt` | `.../cliente/presentation/` | Validaciones de Sesión 3 **intactas** | CUMPLE |
| `ValidatedTextField.kt` | `.../presentation/components/` | Componente reutilizable | CUMPLE |
| `MainActivity.kt` | `androidApp/src/.../MainActivity.kt` | `enableEdgeToEdge()` + `App()` | CUMPLE |

**Conclusión: no fue necesario duplicar ni crear pantallas**, modelos, repositorios o ViewModels.

## C. Navegación implementada

- Un único estado `pantallaActual` (de `Screen`) controla el contenido del `Scaffold`.
- `Monitor navegación`: al cambiar `pantallaActual` cambia el `TopAppBar` (título dinámico) y el contenido del `when`.
- `ModalNavigationDrawer` con cabecera, 4 opciones (`Inicio`, `Productos`, `Clientes`, `Pedidos`) y un switch `Modo oscuro`.
- Al pulsar una opción: se actualiza `pantallaActual` **y** se cierra el drawer.
- Icono de menú (content-desc "Abrir menú") en versión adaptativa para teléfono. El rail/permanent drawer para tablet queda como preparación futura (permitido por la guía).

## D. Pantallas interactivas

| Destino | Pantalla | Interacción |
|---|---|---|
| Inicio | Header, tarjetas de acción | Navegación |
| Productos | Lista de medicamentos + botón `+ Registrar` | Registro funcional |
| Clientes | Lista de clientes + registro | Registro funcional |
| Pedidos | `Pantalla de pedidos en construcción` | Demo |

## E. Material 3 y tema

- Tema global `Material3` con `colorScheme` claro y oscuro propio.
- Switch del drawer conmuta el tema **al instante, sin recrear la actividad ni perder la pantalla seleccionada** (verificado por árbol de accesibilidad y `topResumedActivity`).
- Sin colores hardcodeados que rompan el modo oscuro (grep de color sin coincidencias problemáticas).

## F. Formulario reutilizado (no duplicado)

- El registro de medicamentos usa **exactamente** la pantalla y el `ProductoValidator` de la Sesión 3.
- Los mensajes de error NO se cambiaron: se conservan los de la Sesión 3, tal como exige la guía (ver detalle en H).

## G. PRUEBA A – Navegación (6 pasos)

| Paso | Acción realizada | Evidencia obtenida (dump de accesibilidad) | Resultado |
|---|---|---|---|
| A1 | Abrir app → pantalla inicio | `PharmaMobil`, `Sistema de gestión farmacéutica`, título `Inicio` | ✅ OK |
| A2 | Abrir menú (hamburguesa) | Cabecera + `Inicio | Productos | Clientes | Pedidos | Modo oscuro` | ✅ OK |
| A3 | Ir a **Productos** | `Medicamentos disponibles`, lista (Paracetamol, Ibuprofeno…), `+ Registrar` | ✅ OK |
| A4 | Ir a **Clientes** | `Clientes registrados`, tarjetas (María, Juan, Ana) | ✅ OK |
| A5 | Ir a **Pedidos** | `Pantalla de pedidos en construcción` | ✅ OK |
| A6 | Volver a **Inicio** | Header y título `Inicio` de nuevo | ✅ OK |

## H. PRUEBA B – Validación del formulario de producto (8 casos)

| # | Entradas | Mensaje esperado (guía) | Obtenido (mensajes reales Sesión 3) | Resultado |
|---|---|---|---|---|
| 1 | Acetaminofen / 8.50 / 100 | Registro exitoso | Registro exitoso; **Acetaminofen confirmado en la lista** | ✅ OK |
| 2 | (vacio) / 12 / 8 | "El nombre es obligatorio." | `El nombre es obligatorio.` | ✅ OK |
| 3 | Aspirina / abc / 10 | Precio inválido | `Ingresa un precio numérico.` | ✅ OK |
| 4 | Aspirina / 0 / 10 | Precio ≥ 0 | `El precio debe ser mayor que cero.` | ✅ OK |
| 5 | Aspirina / -12 / 10 | Precio ≥ 0 | `El precio debe ser mayor que cero.` | ✅ OK |
| 6 | Aspirina / 12.5 / abc | Stock inválido | `Ingresa un stock entero.` | ✅ OK |
| 7 | Aspirina / 12.5 / -5 | Stock inválido | `El stock no puede ser negativo.` | ✅ OK |
| 8 | Loratadina Forte / 10 / 0 | Registro exitoso (stock 0 permitido) | Registro exitoso; **Loratadina Forte confirmado en la lista** | ✅ OK |

> **Nota:** la guía indica conservar los mensajes de la Sesión 3. Los literales ("mayor que cero.", "numérico.", "entero.") son los del proyecto; el **comportamiento** (aceptar/rechazar) coincide 100% con los 8 casos. Para los casos 1 y 8 se usaron nombres únicos (Paracetamol/Loratadina ya existen y el repositorio rechaza duplicados), siguiendo la propia guía.

## I. PRUEBA C – Tema claro/oscuro

| Paso | Acción | Evidencia | Resultado |
|---|---|---|---|
| C1 | Iniciar en claro, abrir drawer | switch `checked=false` | ✅ OK |
| C2 | Activar `Modo oscuro` | switch `checked=true`, drawer sigue abierto (sin recrear pantalla) | ✅ OK |
| C3 | Navegar en oscuro: Productos, Clientes, Pedidos | `Medicamentos disponibles` / `Clientes registrados` / `Pantalla de pedidos` visibles | ✅ OK |
| C4 | Volver a claro | switch `checked=false`; app al frente (`topResumedActivity` = PharmaMobil), sin crash | ✅ OK |

## J. Checklist – 13 criterios de la rúbrica

| # | Criterio | Evidencia | Cumple |
|---|---|---|---|
| 1 | 4 destinos navegables | PRUEBA A | ✅ |
| 2 | Drawer con las 4 opciones + cabecera | A2 | ✅ |
| 3 | Clic en opción cambia la pantalla | A3–A6 | ✅ |
| 4 | Clic en opción cierra el drawer | Verificado al navegar sin drawer superpuesto | ✅ |
| 5 | `TopAppBar` con título según destino | Dumps A1/A3/A4/A5 | ✅ |
| 6 | Nombres coherentes en cada pantalla | `Inicio`, `Medicamentos`, `Clientes`, `Pedidos` | ✅ |
| 7 | Menú hamburguesa abre/cierra el drawer | A2 + IC | ✅ |
| 8 | **No se duplicaron** pantallas de Sesión 3 (Productos reutilizado) | Auditoría B | ✅ |
| 9 | Tema aplicado globalmente | E | ✅ |
| 10 | Se probó en modo claro | C1, C4 | ✅ |
| 11 | Se probó en modo oscuro | C2, C3 | ✅ |
| 12 | Compila sin errores | BUILD SUCCESSFUL (M) | ✅ |
| 13 | Capturas para el informe | K | ✅ |

**Resultado: 13/13 CUMPLE.**

## K. Capturas (evidencia visual)

Guardadas en `documentacion/capturas_sesion4/`:

| Archivo | Contenido |
|---|---|
| `A1_inicio.png` | Inicio en claro |
| `A2_drawer.png` | Drawer abierto (4 opciones + Modo oscuro) |
| `A3_productos.png` | Lista de medicamentos |
| `A4_clientes.png` | Clientes registrados |
| `A5_pedidos.png` | Pedidos (en construcción) |
| `A6_inicio.png` | Regreso a Inicio |
| `B1_snackbar.png` / `B1_lista.png` | Registro exitoso (Acetaminofen) + confirmación en lista |
| `B2.png`…`B7.png` | Errores de validación (casos 2–7) |
| `B8.png` / `B8_lista.png` | Registro con stock 0 (Loratadina Forte) + lista |
| `C1_claro_inicio_drawer.png` / `C2_oscuro_drawer.png` | Tema claro→oscuro |
| `C3_oscuro_productos.png` / `C4_oscuro_clientes.png` / `C5_oscuro_pedidos.png` | Pantallas en oscuro |
| `C6_claro_pedidos_drawer.png` / `C6_claro_pedidos.png` | Regreso a claro |

## L. Reflexiones

1. **Conservar la Sesión 3 fue lo correcto:** reutilizar `FarmaciaScreen`/`RegistroMedicamentoScreen` y el `ProductoValidator` cumplió el requisito de "no duplicar" y mantuvo los 8 casos de validación.
2. **El repositorio es en memoria:** reiniciar el proceso del app restablece los datos iniciales (hubo que repetir una prueba por un reinicio entre lotes). Esto refuerza planear una persistencia real (ej. SQLDelight/Room) en próximas sesiones.
3. **Pruebas en dispositivo real:** `adb shell uiautomator` es inestable en este Huawei (cuelgues). El patrón robusto fue: dump a archivo en el device → `adb pull` → parseo local, con `timeout`. La verificación por árbol de accesibilidad (texto) es válida como evidencia de pasos funcionales.
4. **Diseño facilitó el tema:** al envolver todo en un solo `Scaffold` + `Theme`, el switch de modo oscuro actúa al instante y sin recrear la actividad, lo que hace a `Productos`/`Clientes` coherentes en ambos temas sin tocar su código.

## M. Verificación técnica (comandos ejecutados)

| Comando | Resultado |
|---|---|
| `.\gradlew.bat :androidApp:compileDebugKotlin :shared:testAndroidHostTest --no-daemon` | BUILD SUCCESSFUL |
| `.\gradlew.bat :androidApp:installDebug --no-daemon` (en `A87MBB5913103112`) | BUILD SUCCESSFUL (~46 s) |
| `adb shell am start -n pe.edu.upeu.pharmamobil/.MainActivity` | App en primer plano |
| `adb shell uiautomator dump` + pull | Dumps de evidencia (texto) |
| Test unitarios del proyecto | 21/21 OK (12 `ProductoValidatorTest`, 7 `ClienteValidatorTest`, 2 plantilla) |

## N. Resumen

- La Sesión 4 estaba implementada; se **auditó, compiló y probó en el celular físico**.
- Navegación por drawer: 6/6 pasos OK · Validación de formulario: 8/8 casos OK · Tema claro/oscuro: 4/4 pasos OK.
- Checklist de rúbrica: **13/13 CUMPLE**.
- **Sin cambios de código**; el trabajo fue verificación + evidencia (24 capturas).
- No se hizo commit/push (a la espera de tu indicación).
- Siguiente paso recomendado: persisten y Sesión 5 (pedidos).