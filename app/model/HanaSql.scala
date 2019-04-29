package model

import java.sql._

import model.datagen.db.Hana
import play.api.Logger
import play.api.libs.json._

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
	* Created by Guenter Hesse on 13/05/16.
	*/
object HanaSql {

	var numberOfEntriesQuery = "SELECT COUNT(*) FROM \"INDUSTRY\".\"%s\""
	var numberOfEntriesQueryWithWhereClause = "SELECT COUNT(*) FROM \"INDUSTRY\".%s WHERE %s"

	def getRowsAsJson(connection: Connection, table: String, cols: String, whereClause: String = "", objectWithLastRowId: MachineSensorData = null) = {
		val selectStmnt = "SELECT " + cols + " FROM " + table + whereClause
		try {
			//Logger.debug("Executing: " + selectStmnt)
			sqlResultSetToJson(executeQuery(connection, selectStmnt), objectWithLastRowId)
		} catch {
			case e: SQLException => e.printStackTrace
		}
	}

	def getRowsAsResultSet(connection: Connection, table: String, cols: String, whereClause: String = "", objectWithLastRowId: MachineSensorData = null): ResultSet = {
		val selectStmnt = "SELECT " + cols + " FROM " + table + whereClause
		try {
			//Logger.debug("Executing: " + selectStmnt)
			executeQuery(connection, selectStmnt)
		} catch {
			case e: SQLException => e.printStackTrace; null
		}
	}

	def getIntList(connection: Connection, query: String): List[Int] = {
		var tmpRes = ListBuffer[Int]()
		val rs = executeQuery(connection, query)
		while (rs.next()) {
			tmpRes += rs.getInt(1)
		}
		tmpRes.toList
	}

	def getNumberOfEntries(connection: Connection, tables: List[String]): mutable.Map[String, Int] = {
		val resultMap = mutable.Map[String, Int]()
		tables.foreach(table =>
			resultMap(table) = getNumberOfEntriesForSingleTable(connection, table)
		)
		resultMap
	}

	def getNumberOfEntriesForSingleTable(connection: Connection, table: String, whereClause: String = ""): Int = {
		var query = ""
		if (whereClause.isEmpty) {
			query = numberOfEntriesQuery.format(table)
		} else {
			query = numberOfEntriesQueryWithWhereClause.format(table, whereClause)
		}
		executeQueryWhichReturnsSingleNumber(connection, query)
	}

	def getNumberOfEntriesForMultipleTables(connection: Connection, tableList: List[String]):
	Int	= {
		var query = "SELECT COUNT(*) + "
		if (tableList.size < 3) Logger.error("Number of Entries for less than 3 Tables currently not supported!",
			new SQLFeatureNotSupportedException)
		val splitTableList: (List[String], List[String]) = tableList.splitAt(tableList.size - 2)
		for ( table <- splitTableList._1 ) {
			query += s" ( ${numberOfEntriesQuery.format(table)} ) + "
		}
		query += s" (${numberOfEntriesQuery.format(splitTableList._2(0))}) FROM ${Hana.SCHEMA}."
		query += "\"" + splitTableList._2(1) + "\"" //workaround because of bug https://issues.scala-lang.org/browse/SI-6476
		executeQueryWhichReturnsSingleNumber(connection, query)
	}

	def executeQueryWhichReturnsSingleNumber(connection: Connection, query: String): Int = {
		try {
			//Logger.debug("Executing: " + query)
			val resultSet = executeQuery(connection, query)
			resultSet.next()
			resultSet.getInt(1)
		} catch {
			case e: SQLException => {
				Logger.error(e.toString)
				-1
			}
		}
	}

	def executeQuery(connection: Connection, query: String): ResultSet = {
		connection.createStatement().executeQuery(query)
	}

	def executeUpdate(connection: Connection, query: String): Unit = {
		Logger.debug("Executing update/insert: " + query)
		connection.createStatement.executeUpdate(query)
		connection.commit
	}

	def executeUpdates(connection: Connection, queryList: List[String]): Unit = {
		val stmnt: Statement = connection.createStatement
		for(query <- queryList) {
			Logger.debug("Adding following update query to batch: " + query)
			stmnt.addBatch(query)
		}
		stmnt.executeBatch
		connection.commit
	}

	def sqlResultSetToJson(rs: ResultSet, objectWithLastRowId: MachineSensorData = null): JsValue = {
		// This is loosely ported from https://gist.github.com/kdonald/2137988

		val rsmd = rs.getMetaData
		val columnCount = rsmd.getColumnCount

		// It may be faster to collect each line into a Seq or other iterable
		// and pass that to Json.arr() at the end.
		var qJsonArray: JsArray = Json.arr()
		while (rs.next) {
			var index = 1

			var rsJson: JsObject = Json.obj()
			while (index <= columnCount) {
				// Unfortunately jdbc ResultSetMetaData doesn't expose a reliable
				// getTableName method.  It returns the "originalTableName" which doesn't
				// include table aliases defined in the SELECT statement.
				// Therefore the table name needs to be hard coded into each column
				// name in the SELECT command.
				//
				// We should also be checking that there are no duplicate columnLabel's
				// The Json constructors will just mindlessly append items with dup names
				// to the JsObject.
				val column = rsmd.getColumnLabel(index)
				val columnLabel = column.toLowerCase()

				val value = rs.getObject(column)

				if (value == null) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> JsNull
					)
				} else if (value.isInstanceOf[Integer]) {
					val valueAsInt = value.asInstanceOf[Int]
					if (columnLabel.equalsIgnoreCase("id") && valueAsInt > objectWithLastRowId.lastRow) {
						objectWithLastRowId.lastRow = valueAsInt
					}
					rsJson = rsJson ++ Json.obj(
						columnLabel -> valueAsInt
					)
				} else if (value.isInstanceOf[String]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[String]
					)
				} else if (value.isInstanceOf[Boolean]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[Boolean]
					)
				} else if (value.isInstanceOf[Date]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[Date].getTime
					)
				} else if (value.isInstanceOf[Long]) {
					if (columnLabel.equalsIgnoreCase("id")) {
						Logger.error("Got ID w/ datatype long -- currently not supported!")
					}
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[Long]
					)
				} else if (value.isInstanceOf[Double]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[Double]
					)
				} else if (value.isInstanceOf[Float]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[Float]
					)
				} else if (value.isInstanceOf[BigDecimal]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[BigDecimal]
					)
				} else if (value.isInstanceOf[Timestamp]) {
					rsJson = rsJson ++ Json.obj(
						columnLabel -> value.asInstanceOf[Timestamp]
					)
				} else {
					throw new IllegalArgumentException("Unmappable object type: " + value.getClass)
				}
				index += 1
			}
			qJsonArray = qJsonArray :+ rsJson
		}
		qJsonArray
	}

}
