/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import base.SpecBase
import controllers.actions._
import forms.PspDeclarationFormProvider
import models.responses.SubmissionResponse
import models.{NormalMode, QtNumber, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.PspDeclarationPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.PspDeclarationView

import scala.concurrent.Future

class PspDeclarationControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val formProvider = new PspDeclarationFormProvider()
  private val form         = formProvider()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private lazy val pspDeclarationRoute = routes.PspDeclarationController.onPageLoad().url

  val cc = stubControllerComponents()

  val fakeIdentifierAction = new FakeIdentifierActionWithUserType(pspUser, cc.parsers.defaultBodyParser)(cc.executionContext)

  def applicationBuilderPsp(userAnswers: UserAnswers = emptyUserAnswers) = new GuiceApplicationBuilder()
    .overrides(
      bind[IdentifierAction].toInstance(fakeIdentifierAction),
      bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, sessionDataMemberNameQtNumber)),
      bind[SchemeDataAction].to[FakeSchemeDataAction]
    )

  "PspDeclaration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, pspDeclarationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PspDeclarationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockUserAnswersService = mock[UserAnswersService]
      val mockSessionRepository  = mock[SessionRepository]

      when(mockUserAnswersService.submitDeclaration(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(SubmissionResponse(QtNumber("QT123456")))))

      when(mockSessionRepository.set(any()))
        .thenReturn(Future.successful(true))

      val ua = emptyUserAnswers.set(PspDeclarationPage, "A1234567").success.value

      val application = applicationBuilderPsp(userAnswers = ua)
        .overrides(
          bind[SessionRepository].toInstance(mockSessionRepository),
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(POST, pspDeclarationRoute)
            .withFormUrlEncodedBody(("value", "A1234567"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PspDeclarationPage.nextPage(NormalMode, ua).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilderPsp().build()

      running(application) {
        val request =
          FakeRequest(POST, pspDeclarationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PspDeclarationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }

  import org.apache.commons.text.StringEscapeUtils

  "must return Bad Request with association error when PSA is not associated with scheme" in {
    val mockUserAnswersService = mock[UserAnswersService]

    when(mockUserAnswersService.submitDeclaration(any(), any(), any(), any())(any[HeaderCarrier]))
      .thenReturn(Future.failed(new RuntimeException("PSA is not associated with the scheme")))

    val userAnswersWithPspDeclaration = emptyUserAnswers.set(PspDeclarationPage, "A1234567").success.value

    val application = applicationBuilderPsp(userAnswersWithPspDeclaration)
      .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
      .build()

    running(application) {
      val fakePostRequestWithValue = FakeRequest(POST, pspDeclarationRoute)
        .withFormUrlEncodedBody("value" -> "A1234567")

      val resultOfRoute = route(application, fakePostRequestWithValue).value

      status(resultOfRoute) mustBe BAD_REQUEST

      val messagesApi          = messages(application)
      val expectedErrorMessage = messagesApi("pspDeclaration.error.notAssociated")

      val actualContent  = contentAsString(resultOfRoute)
      val decodedContent = StringEscapeUtils.unescapeHtml4(actualContent)

      decodedContent must include(expectedErrorMessage)
    }
  }

}
