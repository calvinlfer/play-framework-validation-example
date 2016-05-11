package models

import play.api.libs.json.{Reads, Writes}

/**
  * Created by calvi on 2016-05-10.
  */
object Gender extends Enumeration {
  type Gender = Value
  val Male, Female = Value

  implicit val enumReads: Reads[Gender] = EnumerationHelpers.enumReads(Gender)
  implicit val enumWrites: Writes[Gender] = EnumerationHelpers.enumWrites
}
