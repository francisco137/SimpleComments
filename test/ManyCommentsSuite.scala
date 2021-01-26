import models.Comment
import org.specs2.mutable._
import play.api.libs.json.JsArray
import play.api.test.Helpers._
import play.api.test._
import scala.concurrent.ExecutionContext
import play.api.libs.json.{JsValue, Json}

class ManyCommentsSuite(implicit ec: ExecutionContext) extends Specification {

  "Application" should {

    "check total number of comments (expected 4700)" in new WithApplication {
      for ( id <- 4774 until 5000) route(app, FakeRequest(DELETE, s"/comments/$id"))
      private val req = route(app, FakeRequest(GET, "/comments"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
      select.length must equalTo(4700)
    }

    "check filtering comments with filter=mama (expected 1)" in new WithApplication {
      private val req = route(app, FakeRequest(GET, "/comments?filter=mama"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
      select.length must equalTo(1)
    }

    "check filtering comments with filter=new york (expected 16)" in new WithApplication {
      private val req = route(app, FakeRequest(GET, "/comments?filter=new york"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
      select.length must equalTo(16)
    }

    "filter comments for few filters (expected specific frequencies)" in new WithApplication() {
      private val lengths = Map("hawaii" -> 6, "new york" -> 16, "los angeles" -> 80, "hospital" -> 4694, "texas" -> 54 )
      for ( len <- lengths ) {
        val filter = len._1
        val req = route(app, FakeRequest(GET, s"/comments?filter=$filter"))
        val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]]
        select.length must equalTo(lengths(filter))
      }
    }

    "check list of comments with filter=new york and sorted by L-method (expected specific list)" in new WithApplication {
      private val newYork12 = List(
        "bellevue hospital center 462 f",
        "harlem hospital center 506 len",
        "hospital for special surgery 5",
        "lenox hill hospital 100 east 7",
        "metropolitan hospital center 1",
        "mount sinai beth israel first ",
        "mount sinai hospital one gusta",
        "mount sinai west 1000 tenth av",
        "n y eye and ear infirmary 230 ",
        "new york community hospital of",
        "new york presbyterian brooklyn",
        "new york presbyterian hospital",
        "new york presbyterian hudson v",
        "new york presbyterian queens 5",
        "nyu langone hospitals 550 firs",
        "rockefeller university hospita"
      )
      private val req = route(app, FakeRequest(GET, "/comments?filter=new york&sort=L"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]].map(_.content.substring(0,30))
      select must equalTo(newYork12)
    }

    "check list of comments with filter=new york and sorted by S-method (expected specific list)" in new WithApplication {
      private val newYork12 = List(
        "\"NEW YORK COMMUNITY HOSPITAL O",
        "BELLEVUE HOSPITAL CENTER 462 F",
        "HARLEM HOSPITAL CENTER 506 LEN",
        "HOSPITAL FOR SPECIAL SURGERY 5",
        "LENOX HILL HOSPITAL 100 EAST 7",
        "METROPOLITAN HOSPITAL CENTER 1",
        "MOUNT SINAI BETH ISRAEL FIRST ",
        "MOUNT SINAI HOSPITAL ONE GUSTA",
        "MOUNT SINAI WEST 1000 TENTH AV",
        "N Y EYE AND EAR INFIRMARY 230 ",
        "NEW YORK-PRESBYTERIAN BROOKLYN",
        "NEW YORK-PRESBYTERIAN HOSPITAL",
        "NEW YORK-PRESBYTERIAN/HUDSON V",
        "NEW YORK-PRESBYTERIAN/QUEENS 5",
        "NYU LANGONE HOSPITALS 550 FIRS",
        "ROCKEFELLER UNIVERSITY HOSPITA"
      )
      private val req = route(app, FakeRequest(GET, "/comments?filter=new york&sort=S"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]].map(_.content.substring(0,30))
      select must equalTo(newYork12)
    }


    "check list of comments with filter=new york and sorted by D-method (expected specific list)" in new WithApplication {
      private val newYork12 = List(
        "100 77th above acute as averag",
        "1000 above acute as avenue ave",
        "1230 acute and are available a",
        "16th above acute as at avenue ",
        "1901 a acute are as available ",
        "1980 above acute as average be",
        "230 a acute and are available ",
        "2525 above acute are as availa",
        "45 56 above acute as average b",
        "462 acute are as available ave",
        "506 above acute as average bel",
        "506 acute are as available ave",
        "525 68th above acute as averag",
        "535 70th a above acute are as ",
        "550 above acute avenue average",
        "above acute as average below c"
      )
      private val req = route(app, FakeRequest(GET, "/comments?filter=new york&sort=D"))
      private val select = Json.toJson(contentAsJson(req.get)).as[JsArray].as[List[Comment]].map(_.content.substring(0,30))
      select must equalTo(newYork12)
    }
  }
}
