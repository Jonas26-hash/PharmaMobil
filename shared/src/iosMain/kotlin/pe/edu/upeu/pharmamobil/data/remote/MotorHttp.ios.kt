package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin

/** iOS usa Darwin (NSURLSession) como motor HTTP. */
actual fun crearMotorHttp(): HttpClientEngine = Darwin.create()