package services.data.dynamodb

import java.util.UUID
import javax.inject.Inject

import models.domain.{Gender, Person}
import services.data.{PersonsRepository, RepositoryError}

import scala.concurrent.Future


/**
  * Note that @Inject is required due to the fact that in order for Guice to work,
  * classes must have either one (and only one) constructor annotated with @Inject
  * or a zero-argument constructor that is not private
  * @param client a fully configured Amazon DynamoDB client
  */
class DynamoDBPersonsRepository @Inject()(client: DynamoDBClient) extends PersonsRepository {
  override def create(person: Person): Future[Either[RepositoryError, Person]] = {
    Future.successful(Right(Person(id = UUID.randomUUID(), "LOL", "BALL", "12345678910", Gender.Male)))
  }

  override def update(person: Person): Future[Either[RepositoryError, Person]] = ???

  override def all: Future[Either[RepositoryError, Seq[Person]]] = ???

  override def delete(personId: UUID): Future[Either[RepositoryError, UUID]] = ???

  override def find(personId: UUID): Future[Either[RepositoryError, Option[Person]]] = ???
}
