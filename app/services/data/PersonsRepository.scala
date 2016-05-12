package services.data

import java.util.UUID

import models.domain.{Person, PersonAlreadyExists, PersonDoesNotExist}

import scala.concurrent.Future

case class CreateResult(person: Person)
case class UpdateResult(person: Person)
case class DeleteResult(deletedId: UUID)

trait PersonsRepository {
  def create(person: Person): Future[Either[PersonAlreadyExists, CreateResult]]

  def read(personId: UUID): Future[Option[Person]]

  def update(person: Person): Future[Either[PersonDoesNotExist, UpdateResult]]

  def delete(personId: UUID): Future[Either[PersonDoesNotExist, DeleteResult]]

  def all: Future[Seq[Person]]
}
