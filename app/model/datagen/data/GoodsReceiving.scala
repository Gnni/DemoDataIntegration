package model.datagen.data

import model.datagen.db.Hana
import model.datagen.util.Helper

object GoodsReceiving {
	val materialsReceived: List[Int] = List(1, 5, 6, 7, 8)
	var GOODS_RECEIVED_TABLE_NAME: String = "\"GOODS_RECEIVED\""
	var GOODS_RECEIVED_POSITION_TABLE_NAME: String = "\"GOODS_RECEIVED_POSITION\""
}

class GoodsReceiving extends InsertObject {
	HEAD_TABLE_NAME = GoodsReceiving.GOODS_RECEIVED_TABLE_NAME
	POSITION_TABLE_NAME = GoodsReceiving.GOODS_RECEIVED_POSITION_TABLE_NAME
	headSql = Hana.INSERT_INTO + Hana.SCHEMA + "." + HEAD_TABLE_NAME + Hana.VALUES_W_BRACKET + "goods_received_seq.NEXTVAL, " + Helper.randBetween(1, 11) + ", '" + Helper.getRandomDateInHanaSeconddateFormat + "')"

	override def getSQLForPositionTable: List[String] = {
		var i: Int = 1
		while (i <= this.numberOfPositions) {
			this.positionSQLList += (Hana.INSERT_INTO + Hana.SCHEMA + "." + POSITION_TABLE_NAME + Hana.VALUES_W_BRACKET + "goods_received_seq.CURRVAL, " + i + ", " + GoodsReceiving.materialsReceived(Helper.randBetween(0, GoodsReceiving.materialsReceived.size - 1)) + ", " + Helper.randBetween(1, 111) + ")")
			i += 1
		}
		this.positionSQLList.toList
	}

}