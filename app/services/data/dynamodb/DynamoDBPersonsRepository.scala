package services.data.dynamodb

import java.util.UUID
import javax.inject.Inject

import cats.data.Xor
import com.gu.scanamo.error.DynamoReadError._
import com.gu.scanamo.error._
import com.gu.scanamo.syntax._
import com.gu.scanamo.{ScanamoAsync, Table}
import models.domain.Person
import play.api.Logger
import services.data.{ConnectionError, PersonsRepository, RepositoryError}
import DynamoDBFormatHelpers._

import scala.concurrent.Future
import scala.language.postfixOps

/**
  * Note that @Inject is required due to the fact that in order for Guice to work,
  * classes must have either one (and only one) constructor annotated with @Inject
  * or a zero-argument constructor that is not private
  *
  * @param client a fully configured Amazon DynamoDB client
  */
class DynamoDBPersonsRepository @Inject()(client: DynamoDBClient) extends PersonsRepository {

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val log = Logger(this.getClass)

  val personsTable = Table[Person](client.prefixedTableName)

  override def create(person: Person): Future[Either[RepositoryError, Person]] =
    try {
      val putRequest = personsTable.put(person)
      val result = ScanamoAsync.exec(client.underlyingClient)(putRequest)
      result.map(_ => Right(person))
    } catch {
      case e: Throwable =>
        log.error(s"Create $person failed", e)
        Future.successful(Left(ConnectionError))
    }


  override def update(person: Person): Future[Either[RepositoryError, Person]] = create(person)

  override def all: Future[Either[RepositoryError, Seq[Person]]] =
    try {
      val result = ScanamoAsync.scan[Person](client.underlyingClient)(client.prefixedTableName)
      val manipulatedResult: Future[Either[RepositoryError, Seq[Person]]] =
      // non-strict => strict (this will cause all the results to be pulled from DynamoDB)
        result.map(_.toList)
          .map(listXor => {
            val badResults = listXor.collect { case Xor.Left(dynamoReadError) => dynamoReadError }
            badResults.foreach((dynamoError: DynamoReadError) => log.error(s"all: ${describe(dynamoError)}"))

            val goodResults = listXor.collect { case Xor.Right(person) => person }
            Right(goodResults)
          })
      // The above assumes optimistic Future composition, here's how to handle the future itself failing
      // So I don't need to try-catch on all operations?
      manipulatedResult.recover {
        case e: Throwable =>
          log.error("all: ", e)
          Left(ConnectionError)
      }
    } catch {
      case e: Throwable =>
        log.error(s"all failed", e)
        Future.successful(Left(ConnectionError))
    }

  override def delete(personId: UUID): Future[Either[RepositoryError, UUID]] =
    try {
      val deleteRequest = personsTable.delete('id -> personId.toString)
      val result = ScanamoAsync.exec(client.underlyingClient)(deleteRequest)
      result.map(_ => Right(personId))
    } catch {
      case e: Throwable =>
        log.error(s"delete $personId failed", e)
        Future.successful(Left(ConnectionError))
    }

  override def find(personId: UUID): Future[Either[RepositoryError, Option[Person]]] =
    try {
      val findRequest = personsTable.get('id -> personId.toString)
      val dynamoResult = ScanamoAsync.exec(client.underlyingClient)(findRequest)
      val xorManipulatedResult = dynamoResult.map(
        (optionXor: Option[Xor[DynamoReadError, Person]]) =>
          optionXor.fold(Xor.right[RepositoryError, Option[Person]](None)) {
            case Xor.Left(dynamoReadError: DynamoReadError) =>
              log.info(describe(dynamoReadError))
              Xor.left[RepositoryError, Option[Person]](ConnectionError)

            case Xor.Right(person: Person) => Xor.right[RepositoryError, Option[Person]](Some(person))
          }
      )
      val finalResult: Future[Either[RepositoryError, Option[Person]]] = xorManipulatedResult.map(_.toEither)
      finalResult
    } catch {
      case e: Throwable =>
        log.error(s"find failed", e)
        Future.successful(Left(ConnectionError))
    }
}
