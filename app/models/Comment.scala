package models

import play.api.libs.json.{Json, OFormat}

case class Comment(id: Long, content: String)

object Comment {
  implicit val commentFormat: OFormat[Comment] = Json.format[Comment]
}
