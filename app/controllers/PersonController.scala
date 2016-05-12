package controllers

import java.util.UUID
import javax.inject.Inject

import com.google.inject.Singleton
import models.domain.Person
import models.dto.ErrorResponse._
import models.dto.{CreatePerson, ErrorResponse}
import play.api.data.validation.ValidationError
import play.api.libs.json.{JsPath, JsResult, Json}
import play.api.mvc.{Action, Controller, Result}
import services.data.{CreateResult, PersonsDAO}

import scala.concurrent.Future

@Singleton
class PersonController @Inject()(persons: PersonsDAO) extends Controller {

  private implicit val ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  private def validateParsedResult[T](jsResult: JsResult[T]): Either[ErrorResponse, T] =
    jsResult.fold(
      (errors: Seq[(JsPath, Seq[ValidationError])]) => {
        val map = fmtValidationResults(errors)
        Left(ErrorResponse("Validation Error", map))
      },
      (t: T) => Right(t)
    )

  private def createPerson(request: CreatePerson): Person =
    Person(UUID.randomUUID(), request.firstName, request.lastName, request.studentId, request.gender)

  private def asyncHttpBadRequestGivenErrorResponse(errorResponse: ErrorResponse): Future[Result] =
    Future.successful(BadRequest(Json toJson errorResponse))

  private def httpBadRequestGivenException(exception: Exception) =
    BadRequest(Json toJson ErrorResponse(code = exception.getMessage, errors = Map.empty))

  private def httpCreatedGivenCreateResult(createResult: CreateResult) =
    Created(Json.toJson(Map[String, String]("status" -> createResult.message, "id" -> createResult.id.toString)))

  private def persistPersonSendHttpResponse(request: CreatePerson): Future[Result] = {
    val person = createPerson(request)
    val dataResult = persons.create(person)
    dataResult.map(_.fold(httpBadRequestGivenException, httpCreatedGivenCreateResult))
  }

  private def personDoesNotExistHttpResponse(personId: UUID): Result =
    NotFound(Json toJson ErrorResponse("Does not Exist", Map("CouldNotFind" -> s"Person with (id: $personId) does not exist")))

  def create = Action.async(parse.json) {
    implicit request =>
      val createPersonRequest = validateParsedResult(request.body.validate[CreatePerson])
      createPersonRequest.fold(asyncHttpBadRequestGivenErrorResponse, persistPersonSendHttpResponse)
  }

  def read(personId: UUID) = Action.async {
    persons.read(personId)
      .map(optPerson =>
        optPerson
          .map(person => Ok(Json.toJson(person)))
          .getOrElse(personDoesNotExistHttpResponse(personId))
      )
  }

  def update(personId: UUID) = TODO

  def delete(personId: UUID) = Action.async {
    persons.delete(personId)
      .map(_.fold(_ => personDoesNotExistHttpResponse(personId), delResult => Ok(delResult.message)))
  }

  def readAll = Action.async(persons.all.map(results => Ok(Json.toJson(results))))
}
