package services.data

import java.util.UUID
import javax.inject.{Inject, Named}

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.model._
import models.domain.{Person, PersonAlreadyExists, PersonDoesNotExist}

import scala.collection.parallel.mutable
import scala.concurrent.Future

/**
  * Note that @Inject is required due to the fact that in order for Guice to work,
  * classes must have either one (and only one) constructor annotated with @Inject
  * or a zero-argument constructor that is not private
  * @param client a fully configured Amazon DynamoDB client
  */
class InMemoryPersonsRepository @Inject() (client: AmazonDynamoDBClient) extends PersonsRepository {
  private val store = mutable.ParTrieMap.empty[UUID, Person]
  private implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  import com.gu.scanamo._
  import com.gu.scanamo.syntax._
//  val createTableRequest = new CreateTableRequest().withTableName("customers")
//  createTableRequest.withKeySchema(new KeySchemaElement().withAttributeName("customerId").withKeyType(KeyType.HASH))
//  createTableRequest.withAttributeDefinitions(new AttributeDefinition().withAttributeName("customerId").withAttributeType(ScalarAttributeType.S))
//  createTableRequest.setProvisionedThroughput(new ProvisionedThroughput(1000L, 1000L))
//  client.createTable(createTableRequest)
  val tables = client.listTables(10)
  println(tables)

  case class Farmer(customerId: String, animals: List[String])
  val putResult = Scanamo.put(client)("customers")(Farmer("Ol Mcdonald", List("sheep", "cow")))
  println(putResult)

  override def create(person: Person): Future[Either[PersonAlreadyExists, CreateResult]] =
    Future {
      if (store.get(person.id).isDefined) {
        Left(PersonAlreadyExists())
      }
      else {
        store += (person.id -> person)
        Right(CreateResult(person))
      }
    }

  override def update(person: Person): Future[Either[PersonDoesNotExist, UpdateResult]] =
    Future {
      store.get(person.id)
        .map(_ => {
          store += (person.id -> person)
          Right(UpdateResult(person))
        }).getOrElse(Left(PersonDoesNotExist()))
    }

  override def all: Future[Seq[Person]] = Future successful store.values.toList

  override def delete(personId: UUID): Future[Either[PersonDoesNotExist, DeleteResult]] =
    Future {
      store.get(personId).map(_ => {
        store remove personId
        Right(DeleteResult(personId))
      }).getOrElse(Left(PersonDoesNotExist()))
    }

  override def read(personId: UUID): Future[Option[Person]] =
    Future {
      store.get(personId)
    }
}
