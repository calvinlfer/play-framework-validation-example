package controllers

import models.dto.{CreatePerson, ErrorResponse}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult, JsValue, Json}
import play.api.mvc.{Action, AnyContent, Controller}
import ErrorResponse._

class PersonController extends Controller {
  def personById(personId: String): Action[AnyContent] = Action {
    Ok(personId)
  }

  def echo: Action[JsValue] = Action(parse.json) {
    implicit request =>
      val eitherPerson = validateParsedResult(request.body.validate[CreatePerson])
      eitherPerson.fold(
        errorResponse => BadRequest(Json.toJson(errorResponse)),
        person => Ok(Json.toJson(person))
      )
  }

  def validateParsedResult[T](jsResult: JsResult[T]): Either[ErrorResponse, T] =
    jsResult.fold(
      (errors: Seq[(JsPath, Seq[ValidationError])]) => {
        val map = fmtValidationResults(errors)
        Left(ErrorResponse("Validation Error", map))
      },
      (t: T) => Right(t)
    )
}
