package models

import play.api.libs.json.Json

case class ErrorResponse(code: String, errors: Map[String, String])

object ErrorResponse {
  implicit val writableJsonErrors = Json.writes[ErrorResponse]
}