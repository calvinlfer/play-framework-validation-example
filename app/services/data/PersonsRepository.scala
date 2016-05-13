package services.data

import java.util.UUID

import models.domain.Person

import scala.concurrent.Future
import scala.util.control.NoStackTrace

case class CreateResult(person: Person)
case class UpdateResult(person: Person)
case class DeleteResult(deletedId: UUID)

case class PersonDoesNotExist(message: String = "Person does not exist")
  extends RuntimeException(message) with NoStackTrace

case class PersonAlreadyExists(message: String = "Person with that ID already exists")
  extends RuntimeException(message) with NoStackTrace

trait PersonsRepository {
  def create(person: Person): Future[Either[PersonAlreadyExists, CreateResult]]

  def read(personId: UUID): Future[Option[Person]]

  def update(person: Person): Future[Either[PersonDoesNotExist, UpdateResult]]

  def delete(personId: UUID): Future[Either[PersonDoesNotExist, DeleteResult]]

  def all: Future[Seq[Person]]
}
