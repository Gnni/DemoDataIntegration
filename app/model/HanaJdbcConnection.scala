package model

import java.sql.{Connection, DriverManager, SQLException}
import model.datagen.db.Hana
import play.api.Logger

/**
	* Created by Guenter Hesse on 09/05/16.
	*/

class HanaJdbcConnection {
	private var _dbConnection : Connection = null

	def dbConnection(): Connection = {
		if (_dbConnection == null || _dbConnection.isClosed) {
			try {
				Logger.info("Establishing JDBC connection to HANA")
				_dbConnection = DriverManager.getConnection(Hana.DEFAULT_HANA_CONNECTION, Hana.USER, Hana.PASSWORD)
			} catch {
				case e: SQLException => {
					e.printStackTrace
				}
			}
		}
		_dbConnection
	}

}
