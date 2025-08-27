package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.freespec.AnyFreeSpec
import views.html.TransferSubmittedView


class TransferSubmittedControllerSpec extends AnyFreeSpec with SpecBase {

  "TransferSubmitted Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, routes.TransferSubmittedController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TransferSubmittedView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}
