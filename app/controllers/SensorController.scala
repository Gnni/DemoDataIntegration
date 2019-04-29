package controllers

import play.api.mvc.{Action, Controller}
import javax.inject._
import model.datagen.config.SensorConfig
import model.datagen.data.DataGenManager
import play.api.Logger
/**
	* Created by guenterhesse on 29/08/16.
	*/
@Singleton
class SensorController @Inject() extends Controller{

	def getNumberOfActiveSensors = Action {
		Logger.info("info::: " + SensorConfig.getSensorConfigList)
		Ok(DataGenManager.getNumberOfSensors.toString)
	}

}
