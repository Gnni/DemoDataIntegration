package model.datagen.data

import model.datagen.config.ConfigForSingleSensor
import model.datagen.db.Hana
import model.datagen.util.Helper
import play.api.Logger

object Sensor {
	val SENSOR_TYPE_TEMP: Int = 0
	val SENSOR_TYPE_NOISE: Int = 1
	val SENSOR_TYPE_VIBRATION: Int = 2
	/*val valueUnitMap: Map[Int, List[Int]] = Map[Int, List[Int]](
		(SENSOR_TYPE_TEMP -> List[Int](-5, 65)),
		(SENSOR_TYPE_NOISE -> List[Int](77, 300)),
		(SENSOR_TYPE_VIBRATION -> List[Int](100, 150))
	)*/
	private[data] val valueUnitMapKeys: Map[Int, String] = Map[Int, String](
		(SENSOR_TYPE_TEMP -> "C"), (SENSOR_TYPE_NOISE -> "dB"), (SENSOR_TYPE_VIBRATION -> "MM")
	)
	var SENSOR_TABLE_NAME: String = "\"MEASURED_DATA\""
	var SENSOR_SEQUENCE_NAME: String = "MEASURED_DATA_SEQ"
}

class Sensor (var sensorConfig: ConfigForSingleSensor) extends InsertObject {
	headSql = null
	this.interval = sensorConfig.durationBetweenMeasurements

	override def getSQLForHeadTable : String = {
		headSql = Hana.INSERT_INTO + Hana.SCHEMA + "." + Sensor.SENSOR_TABLE_NAME + Hana.VALUES_W_BRACKET + Sensor.SENSOR_SEQUENCE_NAME + ".NEXTVAL, " +
			sensorConfig.workplace + ", " + sensorConfig.id + ", '" + Helper.getTSInHanaSeconddateFormat + "'"
		var j: Int = 0
		while (j < 3) {
			if (j != sensorConfig.sensorType) {
				headSql += ",0.0,''"
			} else {
				headSql += "," + Helper.doubleRandBetween(sensorConfig.minValue, sensorConfig.maxValue) + ", '" +
					Sensor.valueUnitMapKeys.get(sensorConfig.sensorType).get + "'"
			}
			j += 1
		}
		headSql += ")"
		headSql
	}

}