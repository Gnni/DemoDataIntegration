package model.datagen.main

import model.datagen.data.DataGenManager
//import org.apache.commons.cli._
import java.sql.SQLException

@Deprecated
object DataGenerator {

	def main(args: Array[String]) {
		try {
			DataGenManager.start
		}
		catch {
			case e: SQLException => {
				e.printStackTrace
			}
		}
	}

	def start(args: Array[String]) = main(args)
	def stop = DataGenManager.stop

}
