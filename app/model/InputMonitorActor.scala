package model

import akka.actor.{Actor, ActorRef, Cancellable, Props}
import play.api.Logger
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
	* Created by guenterhesse on 23/06/16.
	*/

object InputMonitorActor {
	def props(out: ActorRef, datatype: String) = Props(new InputMonitorActor(out, datatype))
}

class InputMonitorActor(out: ActorRef, datatype: String) extends Actor {

	val dataObject = getDataObjectInstance

	def getDataObjectInstance: DataObject = {
		val datatypeInt = datatype.toInt
		datatypeInt match {
			case 0 => return new MachineSensorData()
			case 1 => return new ErpData()
			case 2 => return new MachineSensorData()
		}
	}

	override def preStart() = {
		Logger.info("Starting Input Monitor")
	}

	override def preRestart(reason: Throwable, message: Option[Any]) {
		Logger.error("Restarting due to [" + reason.getMessage + "] when processing [" + message.getOrElse("") + "]")
	}

	def receive = {
		case msg: Int =>
			Logger.debug("Int received! - " + msg)
			if (msg == 0 && !cancellableUpdater.isCancelled) cancellableUpdater.cancel
		case msg: String =>
			Logger.debug("String received! - " + msg)
			if (msg.toInt == 0 && !cancellableUpdater.isCancelled) cancellableUpdater.cancel
		case _ =>
			Logger.debug("Message received!")
	}

	val cancellableUpdater: Cancellable = context.system.scheduler.schedule(
		1.second,
		1.second) {
		val res = dataObject.getNumberOfNewValuesLastSecond
		//if (res > 0) {
			Logger.debug("Sending number of new values - type: " + datatype + "; res: " + res)
			out ! res.toString
		//}

	}

	override def postStop() = {
		cancellableUpdater.cancel
		if (dataObject.dbConnection != null && !dataObject.dbConnection.isClosed) {
			try {
				dataObject.dbConnection.close
			} catch {
				case e: Exception => e.printStackTrace
			}
		}
	}

}
