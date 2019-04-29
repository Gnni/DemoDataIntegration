package model

import akka.actor._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Logger

/**
	* Created by Guenter Hesse on 09/06/16.
	*/

object ChartDataSocketActor {
	def props(out: ActorRef, datatype: String) = Props(new ChartDataSocketActor(out, datatype))
	var updateIntervalMsec: Int = 1000
}

class ChartDataSocketActor(out: ActorRef, datatype: String) extends Actor {

	val machineSensorData = new MachineSensorData(datatype.toInt)
	var cancellableUpdater: Cancellable = getScheduledChartUpdater()

	def startChartUpdatesWithNewInterval(interval: Int = ChartDataSocketActor.updateIntervalMsec) = {
		if (!cancellableUpdater.isCancelled) {
			Logger.debug("Cancelling chart updater")
			cancellableUpdater.cancel
		}
		if (interval > 0) {
			Logger.debug("Starting chart updater")
			cancellableUpdater = getScheduledChartUpdater(interval)
		}
	}

	def stopChartUpdates() = {
		cancellableUpdater.cancel
	}

	def getScheduledChartUpdater(interval: Int = ChartDataSocketActor.updateIntervalMsec) = {
		context.system.scheduler.schedule(
			DurationInt(interval).millisecond,
			DurationInt(interval).millisecond) {
			val res = machineSensorData.getNewRows().toString
			if (!res.equalsIgnoreCase("[]")) {
				//Logger.debug("Sending: " + res)
				out ! res
			}
		}
	}

	override def preStart() = {
		Logger.info("Starting Data Generator")
		out ! machineSensorData.getNewRows().toString
	}

	override def preRestart(reason: Throwable, message: Option[Any]) {
		Logger.error("Restarting due to [" + reason.getMessage + "] when processing [" + message.getOrElse("") + "]")
	}

	def receive = {
		case msg: Int =>
			Logger.debug("Int received! - " + msg)
			startChartUpdatesWithNewInterval(msg)
		case msg: String =>
			Logger.debug("String received! - " + msg)
			startChartUpdatesWithNewInterval(msg.toInt)
		case msg: Any =>
			Logger.debug("Message received! - " + msg)
	}

	override def postStop() = {
		cancellableUpdater.cancel
		if (machineSensorData.dbConnection != null && !machineSensorData.dbConnection.isClosed) {
			try {
				machineSensorData.dbConnection.close
			} catch {
				case e: Exception => e.printStackTrace
			}
		}
	}

}
