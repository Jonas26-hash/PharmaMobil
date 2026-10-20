package pe.edu.upeu.pharmamobil.data.local

import app.cash.sqldelight.driver.native.NativeSqliteDriver
import pe.edu.upeu.pharmamobil.db.PharmaDb

class CreadorDeDriverIos : CreadorDeDriver {

    override fun crear() = NativeSqliteDriver(
        schema = PharmaDb.Schema,
        name = "pharma.db"
    )
}
