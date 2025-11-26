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

package controllers.transferDetails.assetsMiniJourneys.otherAssets

import base.SpecBase
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{NormalMode, UserAnswers}
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
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsSummary
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsCYAView

import scala.concurrent.Future

class OtherAssetsCYAControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val otherAssetsCyaRoute =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsCYAController.onPageLoad(NormalMode, 0).url

  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockSessionRepository  = mock[SessionRepository]

  private def applicationWithMocks(userAnswers: UserAnswers) =
    applicationBuilder(userAnswers = userAnswers)
      .overrides(
        bind[UserAnswersService].toInstance(mockUserAnswersService),
        bind[SessionRepository].toInstance(mockSessionRepository)
      )
      .build()

  "OtherAssetsCYA Controller" - {

    "must return OK and the correct view for a GET" in {
      val ua  = userAnswersWithAssets(assetsCount = 5)
      val app = applicationWithMocks(ua)

      running(app) {
        val request = FakeRequest(GET, otherAssetsCyaRoute)

        val result                  = route(app, request).value
        val view                    = app.injector.instanceOf[OtherAssetsCYAView]
        implicit val msgs: Messages = messages(app)

        val list = OtherAssetsSummary.rows(NormalMode, ua, 0)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(SummaryList(rows = list), NormalMode, 0)(fakeDisplayRequest(request, ua), msgs).toString
      }
    }

    "must redirect to MoreOtherAssetsDeclarationController when threshold (5 other assets) is reached" in {
      val ua  = userAnswersWithAssets(assetsCount = 5)
      val app = applicationWithMocks(ua)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val request = FakeRequest(POST, otherAssetsCyaRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.MoreOtherAssetsDeclarationController
            .onPageLoad(NormalMode)
            .url
      }
    }

    "must redirect to OtherAssetsAmendContinueController when OtherAssets count is below threshold" in {
      val ua  = userAnswersWithAssets(assetsCount = 4)
      val app = applicationWithMocks(ua)

      when(mockUserAnswersService.setExternalUserAnswers(any())(any()))
        .thenReturn(Future.successful(Right(Done)))
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

      running(app) {
        val request = FakeRequest(POST, otherAssetsCyaRoute)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(NormalMode).url
      }
    }
  }
}
