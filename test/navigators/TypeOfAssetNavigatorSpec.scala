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

package navigators

import base.SpecBase
import models.{NormalMode, UserAnswers}
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers.any
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.transferDetails.TypeOfAssetPage
import play.api.mvc.Call
import services.TransferDetailsService

class TypeOfAssetNavigatorSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  private val mockTransferDetailsService = mock[TransferDetailsService]
  private val navigator                  = new TypeOfAssetNavigator(mockTransferDetailsService)

  private val onwardRoute = Call("GET", "/next-page")

  "nextPage" - {

    "must return the next asset route from TransferDetailsService when available" in {
      when(mockTransferDetailsService.getNextAssetRoute(any())) thenReturn Some(onwardRoute)

      val result = navigator.nextPage(TypeOfAssetPage, NormalMode, emptyUserAnswers)

      result mustBe onwardRoute
    }

    "must return IndexController.onPageLoad if TransferDetailsService returns None" in {
      when(mockTransferDetailsService.getNextAssetRoute(any())) thenReturn None

      val result = navigator.nextPage(TypeOfAssetPage, NormalMode, emptyUserAnswers)

      result mustBe controllers.routes.IndexController.onPageLoad()
    }

    "must redirect to JourneyRecoveryController for unknown pages" in {
      val result = navigator.nextPage(FakePage, NormalMode, emptyUserAnswers)

      result mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }
}

object FakePage extends pages.Page {
  override def toString: String = "fakePage"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = controllers.routes.IndexController.onPageLoad()
}
