package pe.edu.upeu.pharmamobil.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

/** Android usa OkHttp como motor HTTP. */
actual fun crearMotorHttp(): HttpClientEngine = OkHttp.create()