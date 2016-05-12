package services.data

import java.util.UUID

import models.domain.{Person, PersonAlreadyExists, PersonDoesNotExist}

import scala.concurrent.Future

case class CreateResult(id: UUID, message: String = "Created")

case class UpdateResult(message: String = "Updated")

case class DeleteResult(message: String = "Deleted")

trait PersonsDAO {
  def create(person: Person): Future[Either[PersonAlreadyExists, CreateResult]]

  def read(personId: UUID): Future[Option[Person]]

  def update(person: Person): Future[Either[PersonDoesNotExist, UpdateResult]]

  def delete(personId: UUID): Future[Either[PersonDoesNotExist, DeleteResult]]

  def all: Future[Seq[Person]]
}
