import models.Comment
import org.scalacheck.{Gen, Prop}
import org.specs2.mutable._
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.ExecutionContext

class OneCommentPropSuite(implicit ec: ExecutionContext) extends Specification {

  "Application" should {

    "check 100 one-comment requests (expected counter = 100)" in new WithApplication() {
      private val check100selects = Gen.choose(1001, 4000)
      var counter = 0
      private val prop = Prop.forAll(check100selects){ id => {
        val req = route(app, FakeRequest(GET, s"/comments/$id"))
        val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
        if ( select.head.id == id ) counter = counter + 1
        true
      }}
      prop.check()
      counter must equalTo(100)
    }

    "check 100 one-comment requests with random prefix (expected prefix in the respond data)" in new WithApplication() {
      private val prefixNumber = Gen.choose(100, 999)
      var counter = 0
      private val prop = Prop.forAll(prefixNumber){ id => {
        val prefix = "px_" + id.toString + "_"
        val req = route(app, FakeRequest(GET, s"/comments/$id?prefix=$prefix"))
        val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
        if ( select.head.content.substring(0,prefix.length) == prefix ) counter = counter + 1
        true
      }}
      prop.check()
      counter must equalTo(100)
    }
  }
}
