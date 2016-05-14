package services.data

import java.util.UUID

import models.domain.Person

import scala.concurrent.Future

sealed trait RepositoryError
case object ConnectionError

sealed trait DeleteStatus
case object SuccessfullyDeleted extends DeleteStatus
// this can definitely be improved since it sort of overlaps with PersonDoesNotExist
case object DoesNotExist extends DeleteStatus

sealed trait CreateStatus
case object SuccessfullyCreated extends CreateStatus
case object PersonAlreadyExists extends CreateStatus

sealed trait UpdateStatus
case object SuccessfullyUpdated extends UpdateStatus
case object PersonDoesNotExist extends UpdateStatus

sealed trait RepositoryResult
case class DeleteResult(status: DeleteStatus, id: UUID) extends RepositoryResult
case class UpdateResult(status: UpdateStatus, optPerson: Option[Person]) extends RepositoryResult
case class CreateResult(status: CreateStatus, optPerson: Option[Person]) extends RepositoryResult

trait PersonsRepository {
  def create(person: Person): Future[Either[RepositoryError, CreateResult]]

  def find(personId: UUID): Future[Either[RepositoryError, Option[Person]]]

  def update(person: Person): Future[Either[RepositoryError, UpdateResult]]

  def delete(personId: UUID): Future[Either[RepositoryError, DeleteResult]]

  def all: Future[Either[RepositoryError, Seq[Person]]]
}
