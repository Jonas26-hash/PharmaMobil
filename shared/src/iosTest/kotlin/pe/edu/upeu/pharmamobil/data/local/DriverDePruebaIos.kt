package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

actual fun crearDriverDePrueba(): SqlDriver =
    NativeSqliteDriver(PharmaDb.Schema, ":memory:")
