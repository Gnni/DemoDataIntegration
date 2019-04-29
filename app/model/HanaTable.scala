package model

/**
	* Created by Guenter Hesse on 13/05/16.
	*/
sealed trait HanaTable {
	def tableName: String
	def valueCol: String = ""
	def whereCol: String = ""
	def whereValue: String = ""
}

case object SalesOrder extends HanaTable {
	val tableName = "SALES_ORDER"
}

case object GoodsReceived extends HanaTable {
	val tableName = "GOODS_RECEIVED"
}
case object MeasuredTemperature extends HanaTable {
	val tableName = "MEASURED_DATA"
	override val valueCol = "TEMPERATURE_VALUE"
	override val whereCol = "TEMPERATURE_UNIT"
}

case object MeasuredVibration extends HanaTable {
	val tableName = "MEASURED_DATA"
	override val valueCol = "VIBRATION_VALUE"
	override val whereCol = "VIBRATION_UNIT"
}

case object MeasuredNoise extends HanaTable {
	val tableName = "MEASURED_DATA"
	override val valueCol = "NOISE_VALUE"
	override val whereCol = "NOISE_UNIT"
}

