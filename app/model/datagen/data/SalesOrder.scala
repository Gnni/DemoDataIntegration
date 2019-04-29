package model.datagen.data

import model.datagen.db.Hana
import model.datagen.util.Helper

object SalesOrder {
	val customerList: List[Int] = List(102, 567, 413, 213, 823, 122)
	var SALES_ORDER_TABLE_NAME: String = "\"SALES_ORDER\""
	var SALES_ORDER_POSITION_TABLE_NAME: String = "\"SALES_ORDER_POSITION\""
	var SALES_ORDER_SEQUENCE_NAME: String = "sales_order_seq"
}

class SalesOrder extends InsertObject {
	HEAD_TABLE_NAME = SalesOrder.SALES_ORDER_TABLE_NAME
	POSITION_TABLE_NAME = SalesOrder.SALES_ORDER_POSITION_TABLE_NAME
	headSql = Hana.INSERT_INTO + Hana.SCHEMA + "." + HEAD_TABLE_NAME + Hana.VALUES_W_BRACKET + SalesOrder.SALES_ORDER_SEQUENCE_NAME + ".NEXTVAL, " + SalesOrder.customerList(Helper.randBetween(0, SalesOrder.customerList.size - 1)) + ", '" + Helper.getRandomDateInHanaSeconddateFormat + "', '" + Helper.getRandomDateInFutureInHanaSeconddateFormat + "')"

	override def getSQLForPositionTable: List[String] = {
		var i: Int = 1
		while (i <= this.numberOfPositions) {
			this.positionSQLList += (Hana.INSERT_INTO + Hana.SCHEMA + "." + POSITION_TABLE_NAME + Hana.VALUES_W_BRACKET + SalesOrder.SALES_ORDER_SEQUENCE_NAME + ".CURRVAL, " + i + ", 4, " + Helper.randBetween(1, 222) + ",0)")
			i += 1
		}
		this.positionSQLList.toList
	}
}