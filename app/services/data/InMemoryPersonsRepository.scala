package services.data

import java.util.UUID

import models.domain.Person

import scala.collection.parallel.mutable
import scala.concurrent.Future


class InMemoryPersonsRepository extends PersonsRepository {
  private val store = mutable.ParTrieMap.empty[UUID, Person]
  private implicit val ec = scala.concurrent.ExecutionContext.Implicits.global


  override def create(person: Person): Future[Either[RepositoryError, CreateResult]] =
    Future {
      if (store.get(person.id).isDefined) {
        Right(CreateResult(PersonAlreadyExists, optPerson = None))
      }
      else {
        store += (person.id -> person)
        Right(CreateResult(SuccessfullyCreated, optPerson = Some(person)))
      }
    }

  override def update(person: Person): Future[Either[RepositoryError, UpdateResult]] =
    Future {
      store.get(person.id)
        .map(_ => {
          store += (person.id -> person)
          Right(UpdateResult(SuccessfullyUpdated, optPerson = Some(person)))
        }).getOrElse(Right(UpdateResult(PersonDoesNotExist, optPerson = None)))
    }

  override def all: Future[Either[RepositoryError, Seq[Person]]] = Future successful Right(store.values.toList)

  override def delete(personId: UUID): Future[Either[RepositoryError, DeleteResult]] =
    Future {
      store.get(personId).map(_ => {
        store remove personId
        Right(DeleteResult(SuccessfullyDeleted, personId))
      }).getOrElse(Right(DeleteResult(DoesNotExist, personId)))
    }

  override def find(personId: UUID): Future[Either[RepositoryError, Option[Person]]] =
    Future {
      Right(store.get(personId))
    }
}
