import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerarUrlApiTask : DefaultTask() {

    @get:Input
    abstract val urlBase: Property<String>

    @get:OutputDirectory
    abstract val directorioDestino: DirectoryProperty

    @TaskAction
    fun generar() {

        val archivo = directorioDestino.get().asFile
            .resolve("pe/edu/upeu/pharmamobil/data/remote/UrlGenerada.kt")

        archivo.parentFile.mkdirs()

        archivo.writeText(
            """
            package pe.edu.upeu.pharmamobil.data.remote

            /**
             * URL base resuelta en tiempo de compilación desde la propiedad de
             * Gradle `pharmaApiBaseUrl`. La usa [ConfigApi].
             */
            internal const val URL_BASE_GENERADA: String = "${urlBase.get()}"

            """.trimIndent()
        )
    }
}
