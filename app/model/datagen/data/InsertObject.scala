package model.datagen.data

import java.util.concurrent.TimeUnit

import model.datagen.config.ConfigForSingleSensor

import scala.collection.mutable.ListBuffer

trait InsertObject {

	var interval: Int = 5000
	var HEAD_TABLE_NAME: String = null
	var POSITION_TABLE_NAME: String = null
	protected var headSql: String = null
	protected var positionSQLList: ListBuffer[String] = ListBuffer[String]()
	var numberOfPositions = 0

	def setInterval(interval: Int) = this.interval = interval
	def setNumberOfPositions(number: Int) = this.numberOfPositions = number
	def getInterval: Int = interval
	def sleep = TimeUnit.MILLISECONDS.sleep(interval)
	def getSQLForHeadTable: String = headSql
	def getSQLForPositionTable: List[String] = positionSQLList.toList
}