package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.freespec.AnyFreeSpec
import views.html.$className$View


class $className$ControllerSpec extends AnyFreeSpec with SpecBase {

  "$className$ Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, routes.$className$Controller.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[$className$View]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}
