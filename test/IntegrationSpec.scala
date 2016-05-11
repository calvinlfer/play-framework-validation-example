import models.Gender.{Gender, Male}
import models.Person
import models.Person._
import org.scalatest.{FunSuite, MustMatchers}
import org.scalatestplus.play._
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.ExecutionContext.{global => globalExecutionContext}

/**
  * add your integration spec here.
  * An integration test will fire up a whole play application in a real (or headless) browser
  */
class IntegrationSpec extends FunSuite with MustMatchers with OneAppPerTest {
  implicit val ec = globalExecutionContext
  test("GET /persons/1234 responds with 1234") {
    val Some(result) = route(app, FakeRequest(GET, "/persons/1234"))
    status(result) mustEqual OK
    contentType(result) mustEqual Some("text/plain")
    charset(result) mustEqual Some("utf-8")
    contentAsString(result) must include("1234")
  }

  test("Sending valid JSON to POST /persons responds with same valid JSON") {
    val examplePerson = Person("Cal", "Fer", "0123456789", Male)
    val Some(result) = route(app, FakeRequest(POST, "/persons").withJsonBody(Json.toJson(examplePerson)))
    status(result) mustEqual OK
    Helpers.contentType(result) mustEqual Some("application/json")

    val responseNode = Json.parse(contentAsString(result))
    (responseNode \ "firstName").as[String] mustEqual "Cal"
    (responseNode \ "lastName").as[String] mustEqual "Fer"
    (responseNode \ "studentId").as[String] mustEqual "0123456789"
    (responseNode \ "gender").as[Gender] mustEqual Male
  }
}
