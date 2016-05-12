package models.domain

import java.util.UUID

import models.domain.Gender.Gender
import play.api.libs.json._

import scala.util.control.NoStackTrace

case class Person(id: UUID, firstName: String, lastName: String, studentId: String, gender: Gender)

object Person {
  implicit val jsonWrites = Json.writes[Person]
}

case class PersonDoesNotExist(message: String = "Person does not exist")
  extends RuntimeException(message) with NoStackTrace

case class PersonAlreadyExists(message: String = "Person with that ID already exists")
  extends RuntimeException(message) with NoStackTrace