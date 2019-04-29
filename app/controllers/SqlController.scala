package controllers

import java.sql.ResultSet
import javax.inject.Inject
import model.HanaSql
import model.datagen.db.Hana
import play.api.mvc.{Action, Controller}

class SqlController @Inject() extends Controller {

  def executeSql = Action { implicit request =>
    val sql = request.body.asFormUrlEncoded.get("sql").mkString
    val resultSet: ResultSet = HanaSql.executeQuery(Hana.getConnection, sql)
    Ok(HanaSql.sqlResultSetToJson(resultSet).toString())
  }

}
