package pe.edu.upeu.pharmamobil.data.remote

/**
 * Configuración única del backend. PharmaMobil no inventa una URL pública:
 * el repositorio pharmaSoft es el código fuente y debe ejecutarse localmente
 * (Spring Boot en el puerto 8080, base path /api/v1).
 *
 * Puntos a recordar según plataforma:
 * - Emulador Android: `10.0.2.2` apunta a la máquina anfitriona.
 * - Dispositivo físico: usar la IP local de la PC dentro de la misma red.
 * - Simulador iOS: `localhost` suele resolver correctamente al host.
 *
 * Este es el ÚNICO lugar donde se declara la base URL. El HttpClient la
 * aplica a todas las peticiones con `defaultRequest`.
 */
object ConfigApi {

    /**
     * Base del backend = http://maquina:8080 + context path /api/v1.
     *
     * No se escribe a mano: la genera la tarea `generarUrlApi` de
     * shared/build.gradle.kts a partir de la propiedad de Gradle
     * `pharmaApiBaseUrl`, de modo que la IP de la máquina del desarrollador no
     * queda en el repositorio. Sin esa propiedad se usa 10.0.2.2, que es lo
     * correcto para el emulador de Android.
     */
    const val BASE_URL = URL_BASE_GENERADA

    /**
     * Context path del backend (server.servlet.context-path en pharmaSoft).
     *
     * Va separado de [BASE_URL] a propósito: Ktor descarta el último segmento
     * del path de la base al resolver una ruta relativa, así que el prefijo se
     * reaplica con appendPathSegments. Si algún día cambia el context path,
     * solo se edita aquí.
     */
    const val PATH_PREFIX = "api/v1"

    /** Primera página que se pide al listar. */
    const val PAGINA_INICIAL = 0

    /** Tamaño de página del primer listado (límite del backend: 100). */
    const val TAMANIO_PAGINA = 100

    /** Campo de ordenamiento por defecto de productos y clientes. */
    const val ORDENAR_POR = "id"

    /** Sentido de ordenamiento por defecto. */
    const val DIRECCION = "asc"
}