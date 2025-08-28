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

package controllers.transferDetails.assetsMiniJourneys.quotedShares

import base.SpecBase
import controllers.routes
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesSummary
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesCYAView

class QuotedSharesCYAControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private lazy val quotedSharesCyaRoute =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesCYAController.onPageLoad(0).url

  "QuotedSharesCYA Controller" - {

    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAssets(assetsCount = 5))).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesCyaRoute)

        val result                  = route(application, request).value
        val view                    = application.injector.instanceOf[QuotedSharesCYAView]
        implicit val msgs: Messages = messages(application)

        val list = QuotedSharesSummary.rows(CheckMode, userAnswersWithAssets(assetsCount = 5), 0)

        status(result) mustEqual OK
        contentAsString(result) mustEqual
          view(SummaryList(rows = list), 0)(fakeDisplayRequest(request, userAnswersWithAssets(assetsCount = 5)), msgs).toString
      }
    }

    "must redirect to MoreQuotedSharesDeclarationController when threshold (5 quoted shares) is reached" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAssets(assetsCount = 5))).build()

      running(application) {
        val request = FakeRequest(POST, quotedSharesCyaRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.MoreQuotedSharesDeclarationController
            .onPageLoad(NormalMode)
            .url
      }
    }

    "must redirect to QuotedSharesAmendContinueController when QuotedShares count is below threshold" in {
      val application = applicationBuilder(userAnswers = Some(userAnswersWithAssets(assetsCount = 4))).build()

      running(application) {
        val request = FakeRequest(POST, quotedSharesCyaRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual
          AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, quotedSharesCyaRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {
      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(POST, quotedSharesCyaRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
