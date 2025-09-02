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

package controllers.transferDetails.assetsMiniJourneys.unquotedShares

import base.SpecBase
import controllers.routes
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{CheckMode, NormalMode, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesSummary
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCYAView

import scala.concurrent.Future

class UnquotedSharesCYAControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val unquotedSharesCyaRoute =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesCYAController.onPageLoad(0).url

  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockSessionRepository  = mock[SessionRepository]

  private def applicationWithMocks(userAnswers: Option[UserAnswers]) =
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "UnquotedSharesCYA Controller" - {

    "must return OK and the correct view for a GET" in {
      val ua  = userAnswersWithAssets(assetsCount = 5)
      val app = applicationWithMocks(Some(ua))

      running(app) {
        val request = FakeRequest(GET, unquotedSharesCyaRoute)

        val result                  = route(app, request).value
        val view                    = app.injector.instanceOf[UnquotedSharesCYAView]
        implicit val msgs: Messages = messages(app)

        val list = UnquotedSharesSummary.rows(CheckMode, ua, 0)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(SummaryList(rows = list), 0)(fakeDisplayRequest(request, ua), msgs).toString
      }
    }

    "must redirect to MoreUnquotedSharesDeclarationController when threshold (5 unquoted shares) is reached" in {
      val ua  = userAnswersWithAssets(assetsCount = 5)
      val app = applicationWithMocks(Some(ua))

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val request = FakeRequest(POST, unquotedSharesCyaRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.MoreUnquotedSharesDeclarationController
            .onPageLoad(NormalMode)
            .url
      }
    }

    "must redirect to UnquotedSharesAmendContinueController when UnquotedShares count is below threshold" in {
      val ua  = userAnswersWithAssets(assetsCount = 4)
      val app = applicationWithMocks(Some(ua))

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val request = FakeRequest(POST, unquotedSharesCyaRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val app = applicationWithMocks(None)

      running(app) {
        val request = FakeRequest(GET, unquotedSharesCyaRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val app = applicationWithMocks(None)

      running(app) {
        val request = FakeRequest(POST, unquotedSharesCyaRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
