package controllers

import java.util.UUID
import javax.inject.Inject

import com.google.inject.Singleton
import models.domain.{Person => PersonModel}
import models.dto.{CreatePerson, ErrorResponse, UpdatePerson}
import models.dto.ErrorResponse._
import models.dto.Person._
import play.api.Logger
import play.api.libs.json._
import play.api.mvc.{Action, Controller, Result}
import services.data._

import scala.concurrent.Future

@Singleton
class PersonController @Inject()(persons: PersonsRepository) extends Controller {
  private val log = Logger(this.getClass)

  private implicit val ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  private def validateJsonBody[T](jsonBody: JsValue)(implicit reads: Reads[T]): Either[ErrorResponse, T] =
    jsonBody.validate[T].asEither.fold(
      errors  =>    Left(ErrorResponse("Validation Error", fmtValidationResults(errors))),
      t       =>    Right(t)
    )

  private def onValidationHandleSuccess[T](jsonBody: JsValue)(successFn: T => Future[Result])
                                    (implicit reads: Reads[T]): Future[Result] =
    validateJsonBody(jsonBody).fold(errorResponse => Future.successful(BadRequest(errorResponse.toJson)), successFn)

  private val ServiceError: Result = ServiceUnavailable(ErrorResponse("Service Error", Map.empty).toJson)

  private def onCreateHandleSuccess[T](futureCreateResult: Future[Either[RepositoryError, CreateResult]])
                                (completeFn: CreateResult => Result): Future[Result] =
    futureCreateResult.map {
      case Right(createResult) => completeFn(createResult)
      case Left(_) => ServiceError
    }

  private def onUpdateHandleSuccess[T](futureUpdateResult: Future[Either[RepositoryError, UpdateResult]])
                                (completeFn: UpdateResult => Result): Future[Result] =
    futureUpdateResult.map {
      case Right(updateResult) => completeFn(updateResult)
      case Left(_) => ServiceError
    }

  private def onFindHandleSuccess[T](futureFindResult: Future[Either[RepositoryError, Option[PersonModel]]])
                              (completeFn: PersonModel => Future[Result]): Future[Result] =
    futureFindResult.flatMap {
      case Right(Some(person)) => completeFn(person)
      case Right(None) => Future.successful(NotFound)
      case Left(_) => Future.successful(ServiceError)
    }

  private def onDeleteHandleAll[T](futureDeleteResult: Future[Either[RepositoryError, DeleteResult]]): Future[Result] =
    futureDeleteResult.map {
      case Right(DeleteResult(SuccessfullyDeleted, uuid)) =>
        Ok(Json toJson Map("id" -> uuid.toString))

      case Right(DeleteResult(DoesNotExist, uuid)) =>
        NotFound(ErrorResponse("Resource not found", Map("id" -> uuid.toString)).toJson)

      case Left(_) =>
        ServiceError
    }

  def create = Action.async(parse.json) {
    implicit request =>
      log.info("POST /persons")
      onValidationHandleSuccess[CreatePerson](request.body) {
        createPersonRequest => {
          val personModel: PersonModel = createPersonRequest.toModel
          onCreateHandleSuccess(persons.create(personModel)) {
            case CreateResult(SuccessfullyCreated, Some(person)) =>
              Ok(person.toDTO.toJson)

            case CreateResult(PersonAlreadyExists, None) =>
              Conflict(ErrorResponse("Person already exists", Map.empty).toJson)
          }
        }
      }
  }

  def read(personId: UUID) = Action.async {
    log.info(s"GET /persons/$personId")
    Future.successful(Ok)
  }

  def update(personId: UUID) = Action.async(parse.json) {
    implicit request =>
      log.info(s"PUT /persons/$personId")
      onValidationHandleSuccess[UpdatePerson](request.body) {
        updatePersonRequest =>
          onFindHandleSuccess(persons.read(personId)) {
            foundPerson => {
              val updatedPerson = updatePersonRequest.toModel(foundPerson)
              onUpdateHandleSuccess(persons.update(updatedPerson)) {
                case UpdateResult(SuccessfullyUpdated, Some(updatedPersonReturned)) =>
                  Ok(updatedPersonReturned.toDTO.toJson)
                case UpdateResult(SuccessfullyUpdated, None) =>
                  ServiceUnavailable(ErrorResponse("Your request was processed but we cannot show the result", Map.empty).toJson)
                case UpdateResult(PersonDoesNotExist, _) =>
                  Conflict(ErrorResponse(s"Person with ID: $personId does not exist", Map.empty).toJson)
              }
            }
          }
      }
  }

  def delete(personId: UUID) = Action.async {
    log.info(s"DELETE /persons/$personId")
    onDeleteHandleAll(persons.delete(personId))
  }

  def readAll = Action.async {
    log.info(s"GET /persons")
    persons.all
      .map {
        either => either.fold(
          _ => ServiceError,
          persons => {
            val jsonPersons = persons.map(eachPerson => eachPerson.toDTO.toJson)
            Ok(JsArray(jsonPersons))
          }
        )
      }
  }
}
