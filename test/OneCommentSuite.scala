import models.Comment
import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json.{JsArray, JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}

class OneCommentSuite(implicit ec: ExecutionContext) extends Specification {

  "Application" should {

    "check if contentType is \"application/json\"" in new WithApplication {
      private val home = route(app, FakeRequest(GET, "/")).get
      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "application/json")
    }

    "insert new comment (id > 0 expected)" in new WithApplication {
      private val insertData: JsValue = Json.toJson(Map("content" -> "This is my new comment"))
      private val insert = route(app, FakeRequest(POST, "/comments").withJsonBody(insertData))
      (Json.parse(contentAsString(insert.get)) \ "id").as[Int] must greaterThan(0)
    }

    "update existing comment (true expected)" in new WithApplication {
      private val dataBefore = Json.toJson(Map("content" -> "This is comment before update."))
      private val insert = route(app, FakeRequest(POST, "/comments").withJsonBody(dataBefore))

      private val id = (Json.parse(contentAsString(insert.get)) \ "id").as[Int]

      private val dataAfter = Json.toJson(Map("content" -> "This is comment after update."))
      private val update = route(app, FakeRequest(PUT, s"/comments/$id").withJsonBody(dataAfter))
      (Json.parse(contentAsString(update.get)) \ "result").as[Boolean] must equalTo(true)

//      Json.parse(contentAsString(update.get)).as[Boolean]
    }

    "update non existing comment (false expected)" in new WithApplication {
      private val dataBefore = Json.toJson(Map("content" -> "This is comment before update."))
      private val update = route(app, FakeRequest(PUT, s"/comments/999999").withJsonBody(dataBefore))
      (Json.parse(contentAsString(update.get)) \ "result").as[Boolean] must equalTo(false)
//      Json.parse(contentAsString(update.get)).as[Boolean] must equalTo(false)
    }

    "delete existing comment (true expected)" in new WithApplication {
      private val testContent = s"This is comment to delete."
      private val data: JsValue = Json.toJson(Map("content" -> testContent))
      private val insert = route(app, FakeRequest(POST, "/comments").withJsonBody(data))
      private val id = (Json.parse(contentAsString(insert.get)) \ "id").as[Int]
      private val delete = route(app, FakeRequest(DELETE, s"/comments/$id"))
      (Json.parse(contentAsString(delete.get)) \ "result").as[Boolean] must equalTo(true)
//    Json.parse(contentAsString(delete.get)).as[Boolean] must equalTo(true)
    }

    "delete non existing comment (false expected)" in new WithApplication {
      private val delete = route(app, FakeRequest(DELETE, s"/comments/999999"))
      (Json.parse(contentAsString(delete.get)) \ "result").as[Boolean] must equalTo(false)
    }

    "select existing comment (specific content is expected)" in new WithApplication {
      private val req = route(app, FakeRequest(GET, s"/comments/1111"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
      private val substr = "WOOD RIVER MEDICAL CENTER 100 HOSPITAL DRIVE KETCHUM BLAINE"
      select.head.content must contain(substr)
    }

    "select non existing comment (0-length comments list expected)" in new WithApplication {
      private val select = route(app, FakeRequest(GET, s"/comments/888888"))
      (Json.parse(contentAsString(select.get)) \ "result").as[Boolean] must equalTo(false)
    }
  }
}
