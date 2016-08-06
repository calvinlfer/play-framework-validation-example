package models.domain

import java.time.ZonedDateTime
import java.util.UUID

import models.domain.Gender.Gender

case class Person(id: UUID, firstName: String, lastName: String, studentId: String, gender: Gender)
case class WrappedPerson(person: Person, timestamp: ZonedDateTime, version: String)

object Gender extends Enumeration {
  type Gender = Value
  val Male, Female = Value
}
