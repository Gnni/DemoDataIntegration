package model.datagen.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

object Hana {
	val INSERT_INTO: String = "insert into "
	val VALUES_W_BRACKET: String = " values ( "
	val SCHEMA: String = "\"INDUSTRY\""
	val DEFAULT_HANA_CONNECTION: String = "jdbc:XXXXXX/?autocommit=false"
	val USER: String = "USER"
	val PASSWORD: String = "PASSWORD"
	def getConnection = (new Hana()).getConnection
}

class Hana {

	private var connection: Connection = null

	def getConnection: Connection = {
		if (connection == null || connection.isClosed) {
			try {
				connection = DriverManager.getConnection(Hana.DEFAULT_HANA_CONNECTION, Hana.USER, Hana.PASSWORD)
			}
			catch {
				case e: SQLException => {
					System.err.println("Connection Failed. User/Passwd Error?")
				}
			}
		}
		connection
	}
}