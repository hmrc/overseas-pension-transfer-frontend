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

package services

import base.SpecBase
import handlers.AssetThresholdHandler
import models.{SessionData, UserAnswers}
import models.assets.TypeOfAsset
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AsyncFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks._
import org.scalatestplus.mockito.MockitoSugar
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import org.mockito.Mockito.reset

import scala.concurrent.Future

class MoreAssetCompletionServiceSpec
    extends AsyncFreeSpec
    with Matchers
    with MockitoSugar
    with SpecBase
    with BeforeAndAfterEach {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val mockAssetThresholdHandler = mock[AssetThresholdHandler]
  private val mockSessionRepository     = mock[SessionRepository]
  private val mockUserAnswersService    = mock[UserAnswersService]

  private val service = new MoreAssetCompletionService(
    mockAssetThresholdHandler,
    mockSessionRepository,
    mockUserAnswersService
  )

  private val supportedAssets =
    Table("assetType", TypeOfAsset.Property, TypeOfAsset.Other, TypeOfAsset.QuotedShares, TypeOfAsset.UnquotedShares)

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAssetThresholdHandler, mockUserAnswersService, mockSessionRepository)
  }

  "MoreAssetCompletionService" - {

    "completeAsset" - {

      forAll(supportedAssets) { assetType =>
        s"should mark $assetType completed, enrich, persist, and return updated UserAnswers" in {

          val userAnswers: UserAnswers = userAnswersWithAssets(assetsCount = 5)

          val updated: SessionData =
            AssetsMiniJourneyService.setAssetCompleted(emptySessionData, assetType, completed = true).success.value

          when(mockAssetThresholdHandler.handle(any(), any(), any()))
            .thenReturn(userAnswers)

          when(mockUserAnswersService.setExternalUserAnswers(any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(Right(Done)))

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))

          service.completeAsset(userAnswers, emptySessionData, assetType, completed = true, userSelection = Some(true)).map { result =>
            result mustBe updated

            verify(mockAssetThresholdHandler).handle(userAnswers, assetType, Some(true))
            verify(mockUserAnswersService).setExternalUserAnswers(userAnswers)
            verify(mockSessionRepository).set(emptySessionData)

            succeed
          }
        }

        s"should handle case when userSelection is None for $assetType" in {
          val userAnswers          = userAnswersWithAssets()
          val updated: SessionData =
            AssetsMiniJourneyService.setAssetCompleted(emptySessionData, assetType, completed = true).success.value

          when(mockAssetThresholdHandler.handle(any(), any(), any()))
            .thenReturn(userAnswers)

          when(mockUserAnswersService.setExternalUserAnswers(any())(any[HeaderCarrier]))
            .thenReturn(Future.successful(Right(Done)))

          when(mockSessionRepository.set(any()))
            .thenReturn(Future.successful(true))

          service.completeAsset(userAnswers, updated, assetType, completed = true).map { result =>
            result mustBe updated
            verify(mockAssetThresholdHandler).handle(userAnswers, assetType, None)
            succeed
          }
        }
      }

      "should propagate failure when enrichment (threshold handler) fails" in {
        val userAnswers = userAnswersWithAssets()
        val asset       = TypeOfAsset.Property

        when(mockAssetThresholdHandler.handle(any(), any(), any()))
          .thenThrow(new RuntimeException("boom"))

        val completed = service.completeAsset(userAnswers, emptySessionData, asset, completed = true)

        recoverToExceptionIf[RuntimeException](completed).map { ex =>
          ex.getMessage mustBe "boom"
          verify(mockUserAnswersService, never()).setExternalUserAnswers(any())(any[HeaderCarrier])
          verify(mockSessionRepository, never()).set(any())
          succeed
        }
      }

      "should throw IllegalArgumentException for unsupported asset type Cash" in {
        val userAnswers = emptyUserAnswers
        val asset       = TypeOfAsset.Cash

        recoverToExceptionIf[IllegalArgumentException] {
          service.completeAsset(userAnswers, emptySessionData, asset, completed = true)
        } map { ex =>
          ex.getMessage mustBe "Cash assets not supported for threshold handling"
        }
      }
    }
  }
}
