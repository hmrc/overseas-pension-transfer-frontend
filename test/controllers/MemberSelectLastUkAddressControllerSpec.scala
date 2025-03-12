package controllers

import base.SpecBase
import forms.MemberSelectLastUkAddressFormProvider
import models.{MemberSelectLastUkAddress, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.MemberSelectLastUkAddressPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.MemberSelectLastUkAddressView

import scala.concurrent.Future

class MemberSelectLastUkAddressControllerSpec extends SpecBase with MockitoSugar {

  private lazy val memberSelectLastUkAddressRoute = routes.MemberSelectLastUkAddressController.onPageLoad(NormalMode).url

  private val formProvider = new MemberSelectLastUkAddressFormProvider()
  private val form         = formProvider()

  "MemberSelectLastUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MemberSelectLastUkAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(MemberSelectLastUkAddressPage, MemberSelectLastUkAddress.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val view = application.injector.instanceOf[MemberSelectLastUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(MemberSelectLastUkAddress.values.head), NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", MemberSelectLastUkAddress.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MemberSelectLastUkAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[MemberSelectLastUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", MemberSelectLastUkAddress.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
