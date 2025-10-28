package controllers

import base.SpecBase
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.scalatest.freespec.AnyFreeSpec
import views.html.SubmittedVersionSummaryView


class SubmittedVersionSummaryControllerSpec extends AnyFreeSpec with SpecBase {

  "SubmittedVersionSummary Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, routes.SubmittedVersionSummaryController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SubmittedVersionSummaryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view()(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}
