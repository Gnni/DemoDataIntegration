package model

import java.sql.Connection

import model.datagen.data.Sensor

/**
	* Created by Guenter Hesse on 14/06/16.
	*/

trait DataObject {
	def getNumberOfNewValuesLastSecond: Int
	def dbConnection: Connection
}

object MachineSensorData {
	val idDateWorkplaceCols = " ID, DATE, WORKPLACE_ID, "
}

class MachineSensorData(datatype: Int = Sensor.SENSOR_TYPE_TEMP) extends DataObject{

	var hanaJdbcConnection: HanaJdbcConnection = new HanaJdbcConnection
	override def dbConnection: Connection = { hanaJdbcConnection.dbConnection() }
	var lastRow = 0

	override def getNumberOfNewValuesLastSecond(): Int = {
		//TODO: remove!
		100 * HanaSql.getNumberOfEntriesForSingleTable(dbConnection, Sensor.SENSOR_TABLE_NAME, "SECONDS_BETWEEN(DATE, CURRENT_TIMESTAMP) < 1")
	}

	def getNewRows() = {

		var table: HanaTable = MeasuredTemperature
		if (datatype == Sensor.SENSOR_TYPE_VIBRATION) {
			table = MeasuredVibration
		} else if (datatype == Sensor.SENSOR_TYPE_NOISE) {
			table = MeasuredNoise
		}

		if (lastRow == 0) {
			HanaSql.getRowsAsJson(dbConnection, "\"INDUSTRY\".\""+ table.tableName +"\"", " TOP 1  " + MachineSensorData.idDateWorkplaceCols + table.valueCol, " WHERE " + table.whereCol + "<>'' ORDER BY DATE DESC", this)
		} else {
			HanaSql.getRowsAsJson(dbConnection, "\"INDUSTRY\".\""+ table.tableName +"\" ", MachineSensorData.idDateWorkplaceCols + table.valueCol, " WHERE " + table.whereCol + "<>'' AND ID > " + lastRow + " ORDER BY DATE ASC", this)
		}

	}

}
