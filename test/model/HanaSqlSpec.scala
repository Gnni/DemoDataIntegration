package model

import org.scalatest._

/**
	* Created by Guenter Hesse on 01/06/16.
	*/
class HanaSqlSpec extends FlatSpec with Matchers {

	"HanaSql" should "contain the correct query for selecting the number of entries of a table" in {
		assert(HanaSql.numberOfEntriesQuery.equalsIgnoreCase("SELECT COUNT(*) FROM \"INDUSTRY\".\"%s\""))
	}

	it should "contain the correct query w/ where clause for selecting the number of entries of a table" in {
		assert(HanaSql.numberOfEntriesQueryWithWhereClause.equalsIgnoreCase("SELECT COUNT(*) FROM \"INDUSTRY\".%s WHERE %s"))
	}

	it should "return 3 for number of entries in table MATERIAL_TYPE" in {
		assert(3 == HanaSql.getNumberOfEntriesForSingleTable(new HanaJdbcConnection().dbConnection, "MATERIAL_TYPE"))
	}

	it should "return 3 twice for number of entries in table MATERIAL_TYPE and MATERIAL_TYPE" in {
		val tableList = List("MATERIAL_TYPE", "TASK")
		val tableNoeMap = collection.immutable.HashMap(tableList(0) -> 3, tableList(1) -> 7)
		val numberOfEntriesList = HanaSql.getNumberOfEntries(new HanaJdbcConnection().dbConnection, tableList)
		assert(numberOfEntriesList.size == 2)
		for ((table, noe) <- numberOfEntriesList) {
			assert(tableList.contains(table))
   		assert(noe == (tableNoeMap.get(table).getOrElse(0)))
		}
	}

}
