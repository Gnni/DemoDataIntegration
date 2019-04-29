package model

import java.sql.{Connection, ResultSet}
import java.util.Date

import model.datagen.data.ProductionOrderWorker
import model.datagen.util.Helper
import play.api.Logger

import scala.collection.mutable.ListBuffer

/**
	* Created by guenterhesse on 28/06/16.
	*/

class ProductionOrderMachine(machineId: Int) extends Runnable {

	val SCHEMA = "\"INDUSTRY\""
	val PRODUCTION_ORDER_POS_TABLE = s"""$SCHEMA.\"PRODUCTION_ORDER_POSITION\""""
	val PRODUCTION_ORDER_TABLE = s"""$SCHEMA.\"PRODUCTION_ORDER\""""
	val SALES_ORDER_POS_TABLE = s"""$SCHEMA.\"SALES_ORDER_POSITION\""""
	val SALES_ORDER_TABLE = s"""$SCHEMA.\"SALES_ORDER\""""
	val BILL_OF_OPERATIONS_TABLE = s"""$SCHEMA.\"BILL_OF_OPERATIONS\""""
	val GOODS_DELIVERED_TABLE = s"""$SCHEMA.\"GOODS_DELIVERED\""""
	val GOODS_DELIVERED_POS_TABLE = s"""$SCHEMA.\"GOODS_DELIVERED_POSITION\""""
	val GOODS_RECEIVED_TABLE = s"""$SCHEMA.\"GOODS_RECEIVED\""""
	val GOODS_RECEIVED_POS_TABLE = s"""$SCHEMA.\"GOODS_RECEIVED_POSITION\""""
	val MAT_ID_STEEL = 1

	var thread: Thread = null
	var connection: Connection = new HanaJdbcConnection().dbConnection
	var durationSpent: Int = 0
	var durationNeeded: Int = 0
	var poId: Int = 0
	var soId: Int = 0
	var soPositionId: Int = 0
	var positionNumber: Int = 0
	var poPositionType: Int = 0
	var goodsReceivedId: Int = 0
	var goodsReceivedPositionNumber: Int = 0
	var billOfOperationsNumber: Int = 0
	var workStep: Int = 0
	var status, quantity, task: Int = 0
	var endTs: Date = null
	var startTs: Date = new Date()
	var avlblMatLastPos: Int = -1

	def setAttributes(resultSet: ResultSet) = {
		poId = resultSet.getInt(1)
		soId = resultSet.getInt(2)
		soPositionId = resultSet.getInt(3)
		positionNumber = resultSet.getInt(4)
		poPositionType = resultSet.getInt(5)
		goodsReceivedId = resultSet.getInt(6)
		goodsReceivedPositionNumber = resultSet.getInt(7)
		billOfOperationsNumber = resultSet.getInt(8)
		workStep = resultSet.getInt(9)
		status = resultSet.getInt(10)
		startTs = new Date()
		endTs = resultSet.getDate(12)
		durationSpent = resultSet.getInt(13)
		quantity = resultSet.getInt(14)
		task = resultSet.getInt(16)
		val durationNeededRs: ResultSet = HanaSql.executeQuery(connection,
			s"SELECT DURATION FROM $BILL_OF_OPERATIONS_TABLE WHERE BILL_OF_OPERATIONS_NUMBER = ${resultSet.getInt(8)} "
				+ s" AND	WORK_STEP = ${resultSet.getInt(9)}" )
		durationNeededRs.next
		durationNeeded = durationNeededRs.getInt(1) * 10 //TODO
	}

	def run: Unit = {
		try {
			while (true) {
				val openProductionOrderOnMachine: ResultSet = HanaSql.executeQuery(connection,
					s"SELECT TOP 1 * FROM $PRODUCTION_ORDER_POS_TABLE WHERE STATUS = ${ProductionOrderWorker.open} " +
						s" and WORKPLACE_ACTUAL = $machineId ORDER BY START_TS ASC") //FIFO

				if (!openProductionOrderOnMachine.isBeforeFirst) {
					//Logger.debug(s"No open orders for machine $machineId")
					if (machineId == 2)
						putNewProductInQueue
					Thread.sleep(4000)
				} else {

					openProductionOrderOnMachine.next
					setAttributes(openProductionOrderOnMachine)
					status = ProductionOrderWorker.open
					while (status != ProductionOrderWorker.done) {
						Thread.sleep(4000)
						//Logger.debug(
						//	s"duration needed: $durationNeeded and spent: $durationSpent and time diff since start: " +
						//		((new Date().getTime - startTs.getTime) / 1000).toInt)
						if ((durationSpent + ((new Date().getTime - startTs.getTime) / 1000).toInt) >= durationNeeded) {
							status = ProductionOrderWorker.done
							updateDuration
						}
					}
					Logger.debug("calling forward product for mach "+ machineId + " and finished is " + status)
					forwardProduct
				}
			}
		} catch {
			case e: InterruptedException => Logger.warn("Sleep interrupted!")
		}
	}

	def updateDuration = {
		HanaSql.executeUpdate(connection, s"UPDATE $PRODUCTION_ORDER_POS_TABLE SET END_TS = '" +
			Helper.getTSInHanaSeconddateFormat + "', DURATION_ACTUAL = " + (durationSpent + ((new Date().getTime - startTs.getTime) / 1000).toInt) +
			s", STATUS = $status WHERE PRODUCTION_ORDER_ID = $poId AND SALES_ORDER_ID = $soId AND SALES_ORDER_POSITION_ID =" +
			s" $soPositionId AND POSITION_NUMBER = $positionNumber")
	}

	def forwardProduct = {
		if (machineId == 4) {
			sendProductToCustomer
		} else {
			//insert
			var nextTaskId = 3
			if (machineId == 3)
				nextTaskId = 4
			val insertPositionSql: String =
				s"INSERT INTO $PRODUCTION_ORDER_POS_TABLE VALUES( $poId, $soId, $soPositionId, ${positionNumber + 1}, " +
					s"$poPositionType, $goodsReceivedId, $goodsReceivedPositionNumber, ${billOfOperationsNumber + 1}," +
					s"$workStep, ${ProductionOrderWorker.open}, '${Helper.getTSInHanaSeconddateFormat}', '" +
					Helper.getTSInHanaSeconddateFormat + s"', 0, $quantity, ${machineId + 1}, $nextTaskId )"
			HanaSql.executeUpdate(connection, insertPositionSql)
		}
	}

	def sendProductToCustomer = {
		val customerRs: ResultSet = HanaSql.executeQuery(connection,
			s"SELECT CUSTOMER FROM $SALES_ORDER_TABLE WHERE ID = $soId")
		if (!customerRs.isBeforeFirst) {
			Logger.error("Didnt find SO w/ ID " + soId)
		}
		customerRs.next
		val customerId = customerRs.getInt(1)
		val insertHeadSql: String = s"INSERT INTO $GOODS_DELIVERED_TABLE VALUES( goods_delivered_seq.NEXTVAL, " +
			s"$customerId, '${Helper.getTSInHanaSeconddateFormat}')"
		val insertPositionSql: String =
			s"INSERT INTO $GOODS_DELIVERED_POS_TABLE VALUES( goods_delivered_seq.CURRVAL, 4, $soId, $soPositionId, " +
				s" $poId, 1)"
		HanaSql.executeUpdates(connection, List(insertHeadSql, insertPositionSql))
	}

	def putNewProductInQueue = {
		val lastlyUsedSoIdAndPosition: List[Int] = getLastSalesOrderIdAndPosition
		val salesOrderId = lastlyUsedSoIdAndPosition(0)
		val soPosId = lastlyUsedSoIdAndPosition(1)
		val sql = s"SELECT QUANTITY - ( SELECT COUNT(*) FROM $PRODUCTION_ORDER_POS_TABLE WHERE " +
			s" BILL_OF_OPERATIONS_NUMBER = 1 AND SALES_ORDER_ID = $salesOrderId AND SALES_ORDER_POSITION_ID = $soPosId ) " +
			s" FROM $SALES_ORDER_POS_TABLE WHERE SALES_ORDER_ID = $salesOrderId AND POSITION_NUMBER = $soPosId"
		val rs: ResultSet = HanaSql.executeQuery(connection, sql)
		rs.next
		val openProductsForCurrentSO: Int = rs.getInt(1)
		if (openProductsForCurrentSO > 0 || lastlyUsedSoIdAndPosition(2) == 1) {
			insertNewProductionOrder(soPosId, salesOrderId)
		} else {
			checkIfNewSONeededAndInsertNewProductionOrder(soPosId, salesOrderId)
		}
	}

	def getLastSalesOrderIdAndPosition: List[Int] = {
		if (soId > 0) {
			List[Int] (soId, soPositionId, 0)
		} else {
			val rs: ResultSet = HanaSql.executeQuery(connection,
				s"SELECT TOP 1 SALES_ORDER_ID, SALES_ORDER_POSITION_ID FROM $PRODUCTION_ORDER_POS_TABLE WHERE " +
				"BILL_OF_OPERATIONS_NUMBER = 1 ORDER BY START_TS DESC")
			if (!rs.isBeforeFirst) {
				getInitialSalesOrderIdAndPosition
			} else {
				rs.next
				List[Int] (rs.getInt(1), rs.getInt(2), 0)
			}
		}
	}

	def getInitialSalesOrderIdAndPosition: List[Int] = {
		val rs: ResultSet = HanaSql.executeQuery(connection,
			s"SELECT TOP 1 SALES_ORDER_ID FROM $SALES_ORDER_POS_TABLE ORDER BY SALES_ORDER_ID ASC")
		rs.next
		List[Int] (rs.getInt(1), 1, 1)
	}

	def checkIfNewSONeededAndInsertNewProductionOrder(salesOrderPositionId: Int = soPositionId, salesOrderId: Int = soId) = {
		val rsNextPosition: ResultSet = HanaSql.executeQuery(connection,
			s"SELECT QUANTITY FROM $SALES_ORDER_POS_TABLE WHERE SALES_ORDER_ID=$salesOrderId AND POSITION_NUMBER=${salesOrderPositionId+1}")
		if (!rsNextPosition.isBeforeFirst) {
			insertNewProductionOrder(1, salesOrderId+1)
		} else {
			insertNewProductionOrder(salesOrderPositionId+1, salesOrderId)
		}
	}

	def insertNewProductionOrder(salesOrderPositionId: Int = soPositionId, salesOrderId: Int = soId) = {
		val goodsReceivedIdAndPositionList: List[Int] = getNextGoodsReceivedAndPositionList
		val insertHeadSql: String =
			s"INSERT INTO $PRODUCTION_ORDER_TABLE VALUES( production_order_seq.NEXTVAL, 4, '" +
				Helper.getTSInHanaSeconddateFormat + "', 1)"
		val insertPositionSql: String =
			s"INSERT INTO $PRODUCTION_ORDER_POS_TABLE VALUES( production_order_seq.CURRVAL, $salesOrderId, " +
				s" $salesOrderPositionId, 1, $poPositionType, ${goodsReceivedIdAndPositionList(0)}, " +
				s" ${goodsReceivedIdAndPositionList(1)}, 1, 1, " +
				s" ${ProductionOrderWorker.open}, '${Helper.getTSInHanaSeconddateFormat}', " +
				s" '${Helper.getTSInHanaSeconddateFormat}', 0, 1, 2, 1)"
		HanaSql.executeUpdates(connection, List(insertHeadSql, insertPositionSql))
	}

	def getNextGoodsReceivedAndPositionList: List[Int] = {
		if (avlblMatLastPos > 0) return List(goodsReceivedId, goodsReceivedPositionNumber)
		getAvlblGoodsReceivedIdAndPosition
	}

	def getAvlblGoodsReceivedIdAndPosition: List[Int] = {
		val rsLastGrIdAndPos: ResultSet = HanaSql.executeQuery(connection,
			"SELECT TOP 1 GOODS_RECEIVED_ID, GOODS_RECEIVED_POSITION_NUMBER FROM " +
			s" $PRODUCTION_ORDER_POS_TABLE WHERE BILL_OF_OPERATIONS_NUMBER = 1 ORDER BY START_TS DESC")
		if(!rsLastGrIdAndPos.isBeforeFirst) {
			val rsFirstGrId: ResultSet = HanaSql.executeQuery(connection,
				s"SELECT TOP 1 GOODS_RECEIVED_ID, POSITION_NUMBER, QUANTITY FROM $GOODS_RECEIVED_POS_TABLE WHERE MATERIAL_ID " +
					s"= $MAT_ID_STEEL ORDER BY GOODS_RECEIVED_ID ASC")
			rsFirstGrId.next
			avlblMatLastPos = rsFirstGrId.getInt(3)
			List[Int](rsFirstGrId.getInt(1), rsFirstGrId.getInt(2))
		} else {
			rsLastGrIdAndPos.next
			val grId = rsLastGrIdAndPos.getInt(1)
			val grPos = rsLastGrIdAndPos.getInt(2)
			avlblMatLastPos = getDiffAvlblMatUsedMat(grId, grPos)
			if (avlblMatLastPos > 0) {
				return List[Int](grId, grPos)
			} else {
				getNextAvlblGoodsReceivedAndPositionList(grId,grPos)
			}
		}
	}

	def getNextAvlblGoodsReceivedAndPositionList(grId : Int, grPos : Int): List[Int] = {
		val rs: ResultSet = HanaSql.executeQuery(connection,
			s"SELECT TOP 1 GOODS_RECEIVED_ID, POSITION_NUMBER, QUANTITY FROM $GOODS_RECEIVED_POS_TABLE WHERE MATERIAL_ID " +
				s"= $MAT_ID_STEEL AND ( (GOODS_RECEIVED_ID = $grId AND POSITION_NUMBER > $grPos) OR (GOODS_RECEIVED_ID > " +
					s"$grId) ) ORDER BY GOODS_RECEIVED_ID ASC")
		rs.next
		avlblMatLastPos = rs.getInt(3)
		List[Int](rs.getInt(1), rs.getInt(2))
	}

	def getDiffAvlblMatUsedMat(goodsReceivedId: Int, goodsReceivedPosition: Int): Int = {
		val rsNumberOfUsedMat: ResultSet = HanaSql.executeQuery(connection,
			s"SELECT QUANTITY - (SELECT COUNT(*) FROM $PRODUCTION_ORDER_POS_TABLE WHERE GOODS_RECEIVED_POSITION_NUMBER = " +
				s"$goodsReceivedPosition AND GOODS_RECEIVED_ID = $goodsReceivedId ) FROM $GOODS_RECEIVED_POS_TABLE WHERE " +
				s" GOODS_RECEIVED_ID = $goodsReceivedId AND POSITION_NUMBER = $goodsReceivedPosition")
		rsNumberOfUsedMat.next
		rsNumberOfUsedMat.getInt(1)
	}

	def start: Unit = {
		Logger.debug(s"Starting Production Order Worker for machine $machineId")
		if (thread == null) {
			thread = new Thread(this)
			thread.start
		}
	}

	def interrupt: Unit = {
		if (status != ProductionOrderWorker.done)
			updateDuration
		thread.interrupt
	}

}
