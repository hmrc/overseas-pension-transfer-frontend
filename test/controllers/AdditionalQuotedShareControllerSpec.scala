package controllers

import base.SpecBase
import forms.AdditionalQuotedShareFormProvider
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import org.scalatest.freespec.AnyFreeSpec
import pages.transferDetails.AdditionalQuotedSharePage
import views.html.AdditionalQuotedShareView

import scala.concurrent.Future

class AdditionalQuotedShareControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new AdditionalQuotedShareFormProvider()
  private val form         = formProvider()

  private lazy val additionalQuotedShareRoute = routes.AdditionalQuotedShareController.onPageLoad(NormalMode).url

  "AdditionalQuotedShare Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request = FakeRequest(GET, additionalQuotedShareRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AdditionalQuotedShareView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = userAnswersQtNumber.set(AdditionalQuotedSharePage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, additionalQuotedShareRoute)

        val view = application.injector.instanceOf[AdditionalQuotedShareView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswersQtNumber))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, additionalQuotedShareRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AdditionalQuotedSharePage.nextPage(NormalMode, userAnswersQtNumber).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request =
          FakeRequest(POST, additionalQuotedShareRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AdditionalQuotedShareView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, additionalQuotedShareRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, additionalQuotedShareRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
