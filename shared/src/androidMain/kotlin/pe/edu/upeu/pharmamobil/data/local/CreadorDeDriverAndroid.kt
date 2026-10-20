package pe.edu.upeu.pharmamobil.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

class CreadorDeDriverAndroid(
    private val contexto: Context
) : CreadorDeDriver {

    override fun crear() = AndroidSqliteDriver(
        schema = PharmaDb.Schema,
        context = contexto,
        name = "pharma.db"
    )
}
