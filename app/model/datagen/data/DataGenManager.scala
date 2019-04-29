package model.datagen.data

import java.sql.SQLException
import java.util.Date
import model.datagen.config.{ConfigForSingleSensor, SensorConfig}

object DataGenManager {
	val START_TS: Date = new Date
	val GOODS_RECEIVING: String = "model.datagen.data.GoodsReceiving"
	val SALES_ORDER: String = "model.datagen.data.SalesOrder"
	val SENSOR: String = "model.datagen.data.Sensor"
	val SCATTER: Double = 0.2
	val SCHEMA: String = "INDUSTRY"
	var isRunning: Boolean = false

	def getNumberOfSensors: Int = {
		var numberOfSensors = 0
		for (inserter <- DataInserter.dataInserterList) {
			if (inserter.isSensor && inserter.isRunning) numberOfSensors += 1
		}
		numberOfSensors
	}

	@throws[SQLException]
	def start = {
		isRunning = true
		new DataInserter(DataGenManager.GOODS_RECEIVING).start
		new DataInserter(DataGenManager.SALES_ORDER).start
		ProductionOrderWorker.startMachines
		for ( sensorConfig <- SensorConfig.getSensorConfigList ) {
			new DataInserter(DataGenManager.SENSOR, sensorConfig).start
		}
	}

	def stop = {
		isRunning = false
		ProductionOrderWorker.interrupt
		DataInserter.interrupt
	}

	def addSensor(sensorConfig: ConfigForSingleSensor) = new DataInserter(DataGenManager.SENSOR, sensorConfig).start

}