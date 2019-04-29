package controllers

import javax.inject.{Inject, Singleton}

import akka.actor.{ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import model.datagen.config.{ConfigForSingleSensor, SensorConfig}
import model.{ChartDataSocketActor, InputMonitorActor}
import model.datagen.data.{DataGenManager, DataInserter, Sensor}
import play.api.libs.streams.ActorFlow
import play.api.mvc.{Action, Controller, WebSocket}
import play.api.Logger

import scala.concurrent.ExecutionContext

/**
	* Created by Guenter Hesse on 07/06/16.
	*/
@Singleton
class DataGeneratorController @Inject() (implicit system: ActorSystem, materializer: Materializer, exec: ExecutionContext) extends Controller {

	def start =  Action {
		Logger.info("Data Generation Started.")
		DataGenManager.start //TODO use config for erp data as well
		NoContent
	}

	def stop =  Action {
		Logger.info("Data Generation Stopped.")
		DataGenManager.stop
		NoContent
	}

	def getData(datatype: String) = WebSocket.accept[String, String] { request =>
		ActorFlow.actorRef(out => ChartDataSocketActor.props(out, datatype))
	}

	def addSensor(numberSensors: Int) = Action {
		/*Logger.info("Adding " + numberSensors + " sensors.")
		val newDataGenManager: DataGenManager = new DataGenManager()
		newDataGenManager.startSensors(numberSensors)*/
		NoContent
	}

	def stopSensor(sensorId: Int) = Action {
		//TODO: implement that ;)
		NoContent
	}

	def addSensor(number: Int, workplace: Int, sensorType: Int, minValue: Double, maxValue: Double, durationBetweenSend: Int) =
		Action {
		if (DataGenManager.isRunning) {
			Logger.info(s"Adding sensor(s): amount: $number; workplace: $workplace; sensor type: $sensorType; min val: $minValue; max val: $maxValue; freq: $durationBetweenSend")
			val sensorIdList: List[Int] = SensorConfig.getSensorIdListForWPAndType(number, workplace, sensorType)
			for (id <- sensorIdList) {
				var wpName: String = "unexpected workplace id"
				workplace match {
					case 2 => wpName = "Cutting Machine"
					case 3 => wpName = "NC Machine"
					case 4 => wpName = "Assembly"
				}
				var stName: String = "unexpected sensor type id id"
				sensorType match {
					case Sensor.SENSOR_TYPE_TEMP => stName = "Temperature"
					case Sensor.SENSOR_TYPE_NOISE => stName = "Noise"
					case Sensor.SENSOR_TYPE_VIBRATION => stName = "Vibration"
				}
				val sensorConfig: ConfigForSingleSensor = new ConfigForSingleSensor(id, workplace, wpName, sensorType, stName, minValue, maxValue,
					durationBetweenSend)
				SensorConfig.addSensorToConfig(sensorConfig)
				new DataInserter(DataGenManager.SENSOR, sensorConfig).start
			}
		} else {
			Logger.warn("You have to start the simulation before adding sensor(s).")
		}
		NoContent
	}

	def setUpAndTestCluster = Action { //TODO: Work in Progress...
		//val actorSys = ActorSystem(system.name, ConfigFactory.load)
		val ports: Seq[String] = Seq("9001", "9002", "0")
		ports foreach { port =>
			val config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + port).
				withFallback(ConfigFactory.load())
			val actorSys = ActorSystem(system.name, config)
			val ref = actorSys.actorOf(Props[SimpleClusterListener], name = s"clusterListener$port")
			Cluster(actorSys).join(Address("akka.tcp", actorSys.name, "localhost", port.toInt))
			ref ! s"...!$port"
		}
		NoContent
	}

	def getNumberOfNewValuesLastSecond(datatype: String) = WebSocket.accept[String, String] { request =>
		ActorFlow.actorRef(out => InputMonitorActor.props(out, datatype))
	}

}
