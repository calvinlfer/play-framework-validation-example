package models.domain

import java.util.UUID

import models.domain.Gender.Gender

case class Person(id: UUID, firstName: String, lastName: String, studentId: String, gender: Gender)
