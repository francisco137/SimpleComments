import org.specs2.mutable._
import play.api.test._
import play.api.test.Helpers._

class HttpClientSuite extends Specification {

  "Application" should {

    "check existance of the index page" in new WithBrowser {
      browser.goTo("http://localhost:" + port )
      browser.pageSource must contain("Welcome to the Machine!")
    }

    "request non-existing path   (NOT_FOUND expected)" in new WithApplication {
      route(app, FakeRequest(GET, "/wrongPath/aa/aas")) must beSome.which (status(_) ==  NOT_FOUND)
    }

    "delete non existing comment (NOT_FOUND expected)" in new WithApplication {
      route(app, FakeRequest(DELETE, "/comments/9999999")) must beSome.which (status(_) ==  NOT_FOUND)
    }

    "select comment with ill defined id (BAD_REQUEST expected)" in new WithApplication {
      route(app, FakeRequest(GET, "/comments/bad_request")) must beSome.which (status(_) ==  BAD_REQUEST)
    }

    "update comment with ill defined id (BAD_REQUEST expected)" in new WithApplication {
      route(app, FakeRequest(PUT, "/comments/bad_request")) must beSome.which (status(_) ==  BAD_REQUEST)
    }

    "delete comment with ill defined id (BAD_REQUEST expected)" in new WithApplication {
      route(app, FakeRequest(DELETE, "/comments/bad_request")) must beSome.which (status(_) ==  BAD_REQUEST)
    }

  }


}
