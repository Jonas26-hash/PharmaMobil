package pe.edu.upeu.pharmamobil

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
