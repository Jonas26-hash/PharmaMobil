package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.db.SqlDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

expect fun crearDriverDePrueba(): SqlDriver

fun crearBaseDeDatosDePrueba(): PharmaDb = PharmaDb(crearDriverDePrueba())
