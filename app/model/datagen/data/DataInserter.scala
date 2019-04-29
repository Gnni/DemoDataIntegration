package model.datagen.data

import java.sql.SQLException
import scala.reflect.runtime.universe._
import scala.reflect.runtime._
import com.sap.db.jdbc.exceptions.jdbc40.SQLIntegrityConstraintViolationException
import model.datagen.util.Helper
import model.HanaJdbcConnection
import model.datagen.config.ConfigForSingleSensor
import play.api.Logger

import scala.collection.mutable.ListBuffer

object DataInserter {
	var dataInserterList: ListBuffer[DataInserter] = ListBuffer[DataInserter]()
	def interrupt = {
		for (inserter <- dataInserterList) {
			inserter.interrupt
		}
	}
}

class DataInserter(var threadName: String, var recPerInterval: Int = 5, var sensorConfig: ConfigForSingleSensor = null) extends Runnable {

	def this(threadName: String, sensorConfig: ConfigForSingleSensor) = this(threadName, -1, sensorConfig)

	DataInserter.dataInserterList += this
	private var thread: Thread = null
	private var connection = new HanaJdbcConnection().dbConnection

	def isRunning: Boolean = thread.isAlive
	def isSensor: Boolean = sensorConfig != null

	def run {
		var sql: String = ""
		try {

			val mirror = universe.runtimeMirror(getClass.getClassLoader)
			val classSymbol = mirror.classSymbol(Class.forName(threadName))
			val constructor = mirror.reflectClass(classSymbol).reflectConstructor(classSymbol.toType.decl(universe.termNames
				.CONSTRUCTOR).asMethod)
			val insertObject: InsertObject = constructor(sensorConfig).asInstanceOf[InsertObject]

			Logger.info("Created object: " + threadName + " with config " + sensorConfig + " see: " + insertObject + " test" +
				" if everything set correctly: 1- config: "  + " 2- interval: " + insertObject.interval)

			while (true) {
				insertObject.sleep
				if (!isSensor) {
					insertObject.setNumberOfPositions(Helper.randBetween(recPerInterval - (recPerInterval * DataGenManager.SCATTER).round, recPerInterval + (recPerInterval * DataGenManager.SCATTER).round))
				}

				sql = insertObject.getSQLForHeadTable
				if (sql != null && !sql.trim.isEmpty) {
					execute(sql)
				}

				val goodsReceivedPositionList: List[String] = insertObject.getSQLForPositionTable
				for (insertPositionSql <- goodsReceivedPositionList) {
					var inserted: Boolean = false
					while (!inserted && !insertPositionSql.trim.isEmpty) {
						try {
							sql = insertPositionSql
							Logger.debug(s"executing: $sql")
							execute(sql)
							inserted = true
						}
						catch {
							case e: SQLIntegrityConstraintViolationException => {
								//Logger.warn("Query " + sql + " failed! " + e + "\n Ignoring query!")
								inserted = true
							}
						}
					}
				}
			}
		}
		catch {
			case e: SQLException => {
				Logger.error("DataInserter: Thread " + thread.getName + " Query " + sql + "failed! " + e)
				e.printStackTrace()
			}
			case e: InterruptedException => {
				Logger.warn("Sleep interrupted: " + e)
			}
			case e: Exception => {
				Logger.error("exception when trying to execute: " + sql)
				e.printStackTrace()
			}
		}
	}

	def execute(query: String): Unit = {
		if (connection == null || connection.isClosed) {
			connection = new HanaJdbcConnection().dbConnection
		}
		connection.createStatement.executeUpdate(query)
		connection.commit
	}

	def start {
		var threadNameWId: String = threadName
		if (isSensor) threadNameWId += sensorConfig.id else threadNameWId += 1
		Logger.debug(s"Starting $threadNameWId")
		if (thread == null) {
			thread = new Thread(this, threadNameWId)
			thread.start
		}
	}

	def interrupt {
		if (thread != null) {
			thread.interrupt
		}
		if (connection != null) {
			try {
				connection.close
			}
			catch {
				case e: SQLException => {
					Logger.warn("Error while trying to close connection of thread " + this.threadName + " - " + e)
				}
			}
		}
	}
}