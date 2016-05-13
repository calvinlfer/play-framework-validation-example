package controllers

import java.util.UUID
import javax.inject.Inject

import com.google.inject.Singleton
import models.dto.ErrorResponse
import models.dto.ErrorResponse._
import models.dto.Person._
import play.api.Logger
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsArray, JsPath, JsResult}
import play.api.mvc.{Action, Controller}
import services.data.PersonsRepository

import scala.concurrent.Future

@Singleton
class PersonController @Inject()(persons: PersonsRepository) extends Controller {
  private val log = Logger(this.getClass)

  private implicit val ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  private def validateParsedResult[T](jsResult: JsResult[T]): Either[ErrorResponse, T] =
    jsResult.fold(
      (errors: Seq[(JsPath, Seq[ValidationError])]) => {
        val map = fmtValidationResults(errors)
        Left(ErrorResponse("Validation Error", map))
      },
      (t: T) => Right(t)
    )


  def create = Action.async(parse.json) {
    implicit request =>
      log.info("POST /persons")
      Future.successful(Ok)
  }

  def read(personId: UUID) = Action.async {
    log.info(s"GET /persons/$personId")
    Future.successful(Ok)
  }

  def update(personId: UUID) = Action.async(parse.json) {
    implicit request =>
      log.info(s"PUT /persons/$personId")
      Future.successful(Ok)
  }

  def delete(personId: UUID) = Action.async {
    log.info(s"DELETE /persons/$personId")
    Future.successful(Ok)
  }

  def readAll = Action.async {
    log.info(s"GET /persons")
    persons.all.map(seqP => seqP.map(_.toDTO.toJson)).map(seqJs => Ok(JsArray(seqJs)))
  }
}
