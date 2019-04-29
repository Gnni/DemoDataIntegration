package model
import java.sql.Connection

/**
	* Created by guenterhesse on 26/08/16.
	*/
object ErpData {
	val BillOfMaterialsTable = "BILL_OF_MATERIAL"
	val BillOfOperationsTable = "BILL_OF_OPERATIONS"
	val GoodsDeliveredTable = "GOODS_DELIVERED"
	val GoodsDeliveredPositionTable = "GOODS_DELIVERED_POSITION"
	val GoodsReceivedTable = "GOODS_RECEIVED"
	val GoodsReceivedPositionTable = "GOODS_RECEIVED_POSITION"
	val MaterialTable = "MATERIAL"
	val MaterialTypeTable = "MATERIAL_TYPE"
	val ProductionOrderTable = "PRODUCTION_ORDER"
	val ProductionOrderPositionTable = "PRODUCTION_ORDER_POSITION"
	val SalesOrderTable = "SALES_ORDER"
	val SalesOrderPositionTable = "SALES_ORDER_POSITION"
	val TaskTable = "TASK"
	val WorkplaceTable = "WORKPLACE"
	val TablesList = List(BillOfMaterialsTable,
		BillOfOperationsTable,
		GoodsDeliveredTable,
		GoodsDeliveredPositionTable,
		GoodsReceivedTable,
		GoodsReceivedPositionTable,
		MaterialTable,
		MaterialTypeTable,
		ProductionOrderTable,
		ProductionOrderPositionTable,
		SalesOrderTable,
		SalesOrderPositionTable,
		TaskTable,
		WorkplaceTable)
	var totalNumberOfRecLastTime: Int = -1
}

class ErpData extends DataObject{

	var hanaJdbcConnection: HanaJdbcConnection = new HanaJdbcConnection
	override def dbConnection: Connection = { hanaJdbcConnection.dbConnection() }

	override def getNumberOfNewValuesLastSecond: Int = {
		var res = 0
		val noEntries = HanaSql.getNumberOfEntriesForMultipleTables(dbConnection, ErpData.TablesList)
		if(ErpData.totalNumberOfRecLastTime != -1) {
			res = noEntries - ErpData.totalNumberOfRecLastTime
		}
		ErpData.totalNumberOfRecLastTime = noEntries
		res
	}

}
