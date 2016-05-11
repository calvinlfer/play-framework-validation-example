package controllers

import models.{ErrorResponse, Person}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult, Json}
import play.api.mvc.{Action, Controller}

class PersonController extends Controller {
  def personById(personId: String) = Action {
    Ok(personId)
  }

  def fmtValidationErrors(errors: Seq[ValidationError]): String =
    errors
      .map(_.message)
      .map {
        case "error.path.missing" => "not provided"
        case normal => normal
      }
      .mkString(", ")

  def fmtValidationResults(errors: Seq[(JsPath, Seq[ValidationError])]): Map[String, String] =
    errors.foldLeft(Map.empty[String, String]) {
      // apply destructuring and then pattern match
      case (resultMap: Map[String, String], (jsPath: JsPath, validationErrors: Seq[ValidationError])) =>
        val fieldWithError = jsPath.path.head.toString.drop(1)
        val errorData = fmtValidationErrors(validationErrors)
        resultMap + (fieldWithError -> errorData)
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
