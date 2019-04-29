package model

import java.sql.Connection
import org.scalatest.{Matchers, FlatSpec}

/**
	* Created by Guenter Hesse on 01/06/16.
	*/
class HanaJdbcConnectionSpec extends FlatSpec with Matchers {

	"HanaJdbcConnection" should "return an connection to hana" in {
		var connection : Connection = null
		try {
			connection = new HanaJdbcConnection().dbConnection
			assert(connection != null)
			assert(connection.isClosed == false)
		} finally {
			connection.close
		}
	}

	it should "return an open connection to hana after old one is closed" in {
		var connection : Connection = null
		try {
			val hanaJdcbConn: HanaJdbcConnection = new HanaJdbcConnection()
			connection = hanaJdcbConn.dbConnection
			assert(connection != null)
			connection.close()
			connection = hanaJdcbConn.dbConnection
			assert(connection != null)
			assert(connection.isClosed == false)
		} finally {
			connection.close
		}
	}

}
