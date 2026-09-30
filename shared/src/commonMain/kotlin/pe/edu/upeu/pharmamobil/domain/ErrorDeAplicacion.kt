package pe.edu.upeu.pharmamobil.domain

/**
 * Error que ya llegó listo para mostrarse al usuario, lanzado por los
 * repositorios remotos cuando la comunicación con el backend falla.
 *
 * La capa de datos nunca expone excepciones de Ktor o de HTTP hacia el
 * dominio ni la presentación: las traduce a este mensaje legible.
 */
class ErrorDeAplicacion(
    mensaje: String
) : Exception(mensaje)