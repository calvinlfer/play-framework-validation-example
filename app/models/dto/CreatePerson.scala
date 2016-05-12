package models.dto

import models.domain.Gender.Gender
import play.api.data.validation.ValidationError
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class CreatePerson(firstName: String, lastName: String, studentId: String, gender: Gender)

object CreatePerson {
  implicit val jsonWrites = Json.writes[CreatePerson]
  implicit val jsonValidatedReads = (
    (JsPath \ "firstName").read[String] //vanilla read followed by additional validators
      .filter(ValidationError("must be more than 2 characters"))(fname => fname.length > 2) and

      (JsPath \ "lastName").read[String]
        .filter(ValidationError("must be more than 2 characters"))(lname => lname.length > 2) and

      (JsPath \ "studentId").read[String]
        .filter(ValidationError("must be 10 digits"))(number => number.length == 10)
        .filter(ValidationError("must be a number"))(number => number.forall(Character.isDigit)) and

      (JsPath \ "gender").read[Gender]

    ) (CreatePerson.apply _)
}
