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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.requests.IdentifierRequest
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import play.api.mvc.Results._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve._

import scala.concurrent.{ExecutionContext, Future}

class IdentifierActionImplSpec extends AnyFreeSpec with SpecBase with MockitoSugar {
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  private val application = new GuiceApplicationBuilder().build()

  private val bodyParsers       = application.injector.instanceOf[BodyParsers.Default]
  private val appConfig         = application.injector.instanceOf[FrontendAppConfig]
  private val mockAuthConnector = mock[AuthConnector]

  private val internalIdValue = "test-user-id"
  private val enrolmentKey    = "HMRC-PODS-ORG"
  private val identifierKey   = "PSAID"
  private val identifierValue = "A1234567"
  private val fakeRequest     = FakeRequest()

  private val action = new IdentifierActionImpl(mockAuthConnector, appConfig, bodyParsers)

  "IdentifierAction" - {

    "must allow access with valid internalId and required enrolment" in {
      val enrolment = Enrolment(
        enrolmentKey,
        Seq(EnrolmentIdentifier(identifierKey, identifierValue)),
        "Activated"
      )

      type RetrievalResult = Option[String] ~ Enrolments

      val expectedRetrieval: RetrievalResult =
        new ~(Some(internalIdValue), Enrolments(Set(enrolment)))

      when(mockAuthConnector.authorise[RetrievalResult](any(), any[Retrieval[RetrievalResult]])(any(), any()))
        .thenReturn(Future.successful(expectedRetrieval))

      val result = action.invokeBlock(
        fakeRequest,
        { request: IdentifierRequest[AnyContent] =>
          Future.successful(Ok(s"OK - ${request.authenticatedUser.internalId} - ${request.authenticatedUser.psaPspId.value}"))
        }
      )

      status(result) mustBe OK
      contentAsString(result) must include(s"OK - $internalIdValue - $identifierValue")
    }

    "must redirect to login if no active session" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(BearerTokenExpired()))

      val result             = action.invokeBlock(fakeRequest, (_: IdentifierRequest[AnyContent]) => fail("Should not reach block"))
      val redirectedLocation = java.net.URLDecoder.decode(redirectLocation(result).get, "UTF-8")

      status(result) mustBe SEE_OTHER
      redirectedLocation mustBe s"${appConfig.loginUrl}?continue=${appConfig.loginContinueUrl}"
    }

    "must redirect to unauthorised page on InsufficientEnrolments" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any())).thenReturn(Future.failed(InsufficientEnrolments()))

      val result = action.invokeBlock(fakeRequest, (_: IdentifierRequest[AnyContent]) => fail("Should not reach block"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.auth.routes.UnauthorisedController.onPageLoad().url)
    }

    "must redirect to unauthorised page on unexpected error" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new RuntimeException("Unexpected error")))

      val result = action.invokeBlock(fakeRequest, (_: IdentifierRequest[AnyContent]) => fail("Should not reach block"))

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.auth.routes.UnauthorisedController.onPageLoad().url)
    }
  }
}
