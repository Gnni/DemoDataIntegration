package model.datagen.config

/**
	* Created by guenterhesse on 30/08/16.
	*/
import model.HanaSql
import model.datagen.db.Hana
import scala.io.Source
import play.api.Logger
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.json._
import scala.collection.mutable.ListBuffer

class ConfigForSingleSensor(val id: Int, val workplace: Int, val workplaceName: String, val sensorType: Int, val sensorTypeName: String, val minValue: Double, val maxValue: Double, val durationBetweenMeasurements: Int)

object SensorConfig {

	private val SensorsSeqId = "sensors"
	private val SensorTypesSeqId = "sensor_types"
	private val SensorConfigId = "sensor_config"
	private val WorkplaceId = "workplace_id"
	private val WorkplaceName = "workplace_name"
	private val SensorTypeId = "sensor_type_id"
	private val SensorTypeName = "sensor_type_name"
	private val ConfigNameId = "config_name"
	private val MinValue = "min_value"
	private val MaxValue = "max_value"
	private val AttributeTypeId = "attribute_type"
	private val AttributeId = "attribute"
	private val ValueId = "value"
	private val RelationId = "relation"
	private val DefectiveGoodConfigId = "defective_good_config"
	private val DurationBetweenMeasurementsInMsId = "duration_between_measurements_in_ms"
	private val DefaultConfigFileLocation: String = "conf/sensor_config.json"
	private var configMap: JsValue = null
	private var config: Config = null
	private var sensorConfigList: List[ConfigForSingleSensor] = null
	val SensorTableWSchema = "\"INDUSTRY\".\"SENSOR\""

	def getConfig(configFileLocation: String = DefaultConfigFileLocation) = {
		if (config == null) load(configFileLocation)
		config
	}

	def load(configFileLocation: String = DefaultConfigFileLocation) = {
			val configSource = Source.fromFile(configFileLocation)
			val configAsString: String = try configSource.mkString finally configSource.close
			configMap = Json.parse(configAsString)
			configMap.validate[Config] match {
				case s: JsSuccess[Config] => Logger.info("Sensor config successfully loaded: " + s.get)
				case e: JsError => Logger.error("Error loading sensor config: " + e)
			}
			config = configMap.as[Config]
	}

	implicit val sensorReads: Reads[Sensor] = (
		(JsPath \ MinValue).read[Double] and
		(JsPath \ MaxValue).read[Double] and
		(JsPath \ DurationBetweenMeasurementsInMsId).read[Int]
		) (Sensor.apply _)

	implicit val sensorTypeReads: Reads[SensorType] = (
		(JsPath \ SensorTypeId).read[Int] and
		(JsPath \ SensorTypeName).read[String] and
		(JsPath \ SensorsSeqId).read[Seq[Sensor]]
		) (SensorType.apply _)

	implicit val workplaceReads: Reads[Workplace] = (
		(JsPath \ WorkplaceId).read[Int] and
		(JsPath \ WorkplaceName).read[String] and
		(JsPath \ SensorTypesSeqId).read[Seq[SensorType]]
		) (Workplace.apply _)

	implicit val defectiveGoodReads: Reads[DefectiveGoodSingleCondition] = (
		(JsPath \ AttributeTypeId).read[Int] and
		(JsPath \ AttributeId).read[String] and
		(JsPath \ ValueId).read[String] and
		(JsPath \ RelationId).read[String]
		) (DefectiveGoodSingleCondition.apply _)

	implicit val configReads: Reads[Config] = (
		(JsPath \ ConfigNameId).read[String] and
		(JsPath \ DefectiveGoodConfigId).read[Seq[DefectiveGoodSingleCondition]] and
		(JsPath \ SensorConfigId).read[Seq[Workplace]]
		) (Config.apply _)

	def getSensorConfigList = {
		if (sensorConfigList == null) {
			val tmpSensorConfigList = ListBuffer[ConfigForSingleSensor]()
			val config = getConfig()
			for (workplace <- config.sensorConfig) {
				for (sensorType <- workplace.sensorTypes) {
					val numberOfSensorsForWPAndST = sensorType.sensors.size
					val idList: List[Int] = getSensorIdListForWPAndType(numberOfSensorsForWPAndST, workplace.id, sensorType.id)
					var idx = 0
					for (sensor <- sensorType.sensors) {
						tmpSensorConfigList += new ConfigForSingleSensor(idList(idx), workplace.id, workplace.name, sensorType.id, sensorType.name, sensor.min_value, sensor.max_value, sensor.durationBetweenMeasurementsInMs)
						sensor.id = idList(idx)
						idx += 1
					}
				}
			}
			sensorConfigList = tmpSensorConfigList.toList
		}
		sensorConfigList
	}

	def addSensorToConfig(sensorConfig: ConfigForSingleSensor) = {
		var added = false
		val newSensor = Sensor(sensorConfig.minValue, sensorConfig.maxValue, sensorConfig.durationBetweenMeasurements)
		newSensor.id = sensorConfig.id

		for (workplace <- config.sensorConfig) {
			if (workplace.id == sensorConfig.workplace) {
				for (sensorType <- workplace.sensorTypes) {
					if (sensorType == sensorConfig.sensorType) {
						sensorType.sensors :+ newSensor
						added = true
					}
				}
				if (!added) {
					//new sensor type for WP
					val newSensorType = SensorType(sensorConfig.sensorType, sensorConfig.sensorTypeName, Seq[Sensor]())
					newSensorType.sensors :+ newSensor
					workplace.sensorTypes :+ newSensorType
				}
			}
		}
		if (!added) {
			//new sensor type for WP
			val newWorkplace = Workplace(sensorConfig.workplace, sensorConfig.workplaceName, Seq[SensorType]())
			val newSensorType = SensorType(sensorConfig.sensorType, sensorConfig.sensorTypeName, Seq[Sensor]())
			newSensorType.sensors :+ newSensor
			newWorkplace.sensorTypes :+ newSensorType
			config.sensorConfig :+ newWorkplace
		}
	}

	def getSensorIdListForWPAndType(number: Int, workplace: Int, typeId: Int): List[Int] = {
		HanaSql.getIntList(Hana.getConnection, s"SELECT TOP $number ID " +
			" FROM "+SensorConfig.SensorTableWSchema+s" WHERE TYPE_ID= $typeId AND WORKPLACE= $workplace "+
			" ORDER BY ID ASC")
	}

}

case class DefectiveGoodSingleCondition(attributeType: Int, attribute: String, value: String, relation: String)
case class Sensor(min_value: Double, max_value: Double, durationBetweenMeasurementsInMs: Int) {
	var id: Int = -1
}
case class SensorType(id: Int, name: String, sensors: Seq[Sensor])
case class Workplace(id: Int, name: String, sensorTypes: Seq[SensorType])
case class Config(name: String, defectiveGoodConfig: Seq[DefectiveGoodSingleCondition], sensorConfig: Seq[Workplace])