package controllers

import models.{ErrorResponse, Person}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult, Json}
import play.api.mvc.{Action, Controller}
import ErrorResponse._

class PersonController extends Controller {
  def personById(personId: String) = Action {
    Ok(personId)
  }

  def echo = Action(parse.json) {
    implicit request =>
      val json: JsResult[Person] = request.body.validate[Person]
      json.fold(
        (errors: Seq[(JsPath, Seq[ValidationError])]) => {
          val map = fmtValidationResults(errors)
          Ok(Json.toJson(ErrorResponse("Validation Error", map)))
        },
        (person: Person) => Ok(Json.toJson(person))
      )
  }
}
