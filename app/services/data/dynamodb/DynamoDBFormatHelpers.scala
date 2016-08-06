package services.data.dynamodb

import java.util.UUID

import com.gu.scanamo._
import models.domain.{Gender, Person}

/**
  * Scanamo format helpers that help with serialization and deserialization of custom formats like
  * Gender Enumerations and UUIDs
  */
object DynamoDBFormatHelpers {
  implicit def personFormat: DynamoFormat[Person] = DynamoFormat.coercedXmap[Person, Map[String, String], Exception](
    map =>
      Person(
        UUID.fromString(map("id")),
        map("firstName"),
        map("lastName"),
        map("studentId"),
        Gender.withName(map("gender"))
      )
  )(
    person =>
      Map(
        "id" -> person.id.toString,
        "firstName" -> person.firstName,
        "lastName" -> person.lastName,
        "studentId" -> person.studentId.toString,
        "gender" -> person.gender.toString
      )
  )
}
