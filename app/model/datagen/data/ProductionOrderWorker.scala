package model.datagen.data

import java.sql.{Connection, ResultSet, SQLException}

import model.{HanaJdbcConnection, HanaSql, ProductionOrderMachine}
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
	* Created by guenterhesse on 28/06/16.
	*/
object ProductionOrderWorker {

	var machineList: ListBuffer[ProductionOrderMachine] = new ListBuffer[ProductionOrderMachine]

	val open: Int = 1
	val done: Int = 0

	def startMachines = {
		for(machineId <- 2 to 4) {
			Logger.info("Starting machine " + machineId)
			val machine = new ProductionOrderMachine(machineId)
			machineList.+=(machine)
			machine.start
		}
	}

	def interrupt = {
		for (machine <- machineList) {
			Logger.debug("Interrupting machine " + machine)
			machine.interrupt
		}
		machineList = new ListBuffer[ProductionOrderMachine]
	}

}
