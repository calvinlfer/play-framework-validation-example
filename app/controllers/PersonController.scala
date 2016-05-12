package controllers

import java.util.UUID
import javax.inject.Inject

import com.google.inject.Singleton
import models.domain.Person
import models.dto.ErrorResponse._
import models.dto.{CreatePerson, ErrorResponse}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult, Json}
import play.api.mvc.{Action, Controller}
import services.data.PersonsDAO

import scala.concurrent.Future

@Singleton
class PersonController @Inject()(persons: PersonsDAO) extends Controller {

  private implicit val ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  def create = Action.async(parse.json) {
    implicit request =>
      val eitherCreatePerson = validateParsedResult(request.body.validate[CreatePerson])
      eitherCreatePerson.fold(
        errorResponse => Future.successful(BadRequest(Json.toJson(errorResponse))), {
          createPersonReq =>
            val uuid = UUID.randomUUID()
            val person = Person(uuid, createPersonReq.firstName, createPersonReq.lastName, createPersonReq.studentId, createPersonReq.gender)
            val result = persons.create(person)

            result.map(either =>
              either.fold(
                error => BadRequest(error.message),
                createResult =>
                  Created(
                    Json.toJson(
                      Map[String, String](
                        "status" -> createResult.message,
                        "id" -> createResult.id.toString
                      )
                    )
                  )
              )
            )
        }
      )
  }

  def read(personId: UUID) = Action.async {
    persons.read(personId)
      .map(optPerson =>
        optPerson
          .map(person => Ok(Json.toJson(person)))
          .getOrElse(NotFound(s"Person with (id: $personId) does not exist"))
      )
  }

  def update(personId: UUID) = TODO

  def delete(personId: UUID) = Action.async {
    persons.delete(personId)
      .map(
        either =>
          either.fold(
            doesNotExist => NotFound(s"Person with (id: $personId) does not exist"),
            deleteResult => Ok(deleteResult.message))
      )
  }

  def readAll = Action.async(persons.all.map(results => Ok(Json.toJson(results))))

  private def validateParsedResult[T](jsResult: JsResult[T]): Either[ErrorResponse, T] =
    jsResult.fold(
      (errors: Seq[(JsPath, Seq[ValidationError])]) => {
        val map = fmtValidationResults(errors)
        Left(ErrorResponse("Validation Error", map))
      },
      (t: T) => Right(t)
    )
}
