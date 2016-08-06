package services.data.dynamodb

import java.nio.ByteBuffer
import java.util.Base64

import com.gu.scanamo._
import models.domain.Gender.Gender
import models.domain.{Gender, Person}

/**
  * Scanamo format helpers that help with serialization and deserialization of custom formats like
  * Gender Enumerations and UUIDs
  */
object DynamoDBFormatHelpers {

  import boopickle.Default._

  implicit val genderPickler = transformPickler((s: String) => Gender.withName(s))((g: Gender) => g.toString)

  implicit def personFormat: DynamoFormat[Person] = DynamoFormat.coercedXmap[Person, Map[String, String], Exception](
    map =>
      Unpickle[Person].fromBytes(ByteBuffer.wrap(Base64.getDecoder.decode(map("data"))))
  )(
    person =>
      Map(
        "id" -> person.id.toString,
        "version" -> "1",
        "data" -> new String(Base64.getEncoder.encode(Pickle.intoBytes(person).array()))
      )
  )
}
