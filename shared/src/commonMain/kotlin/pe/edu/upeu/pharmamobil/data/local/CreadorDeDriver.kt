package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.db.SqlDriver

interface CreadorDeDriver {

    fun crear(): SqlDriver
}
