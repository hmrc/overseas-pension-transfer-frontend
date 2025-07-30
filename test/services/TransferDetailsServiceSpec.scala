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
import models.TypeOfAsset.{reads, writes}
import models.{SharesEntry, TypeOfAsset, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.transferDetails.TypeOfAssetPage
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import queries.assets.{AssetCompletionFlag, UnquotedSharesQuery}
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TransferDetailsServiceSpec extends AnyFreeSpec with SpecBase {

  private val stubSessionRepository = mock[SessionRepository]

  private val service = new TransferDetailsService(stubSessionRepository)

  "assetCount" - {

    "must return the number of entries for an asset type" in {
      val entries     = List(SharesEntry("Foo", 1, "GBP", "Class A"))
      val userAnswers = emptyUserAnswers.set(UnquotedSharesQuery, entries).success.value

      val result = service.assetCount[SharesEntry](userAnswers, TypeOfAsset.UnquotedShares)
      result mustBe 1
    }

    "must return 0 if no entries exist" in {
      val userAnswers = emptyUserAnswers

      val result = service.assetCount[SharesEntry](userAnswers, TypeOfAsset.QuotedShares)
      result mustBe 0
    }
  }

  "removeAssetEntry" - {

    "must remove the specified entry and return updated answers" in {
      val entries     = List(SharesEntry("One", 1, "GBP", "A"), SharesEntry("Two", 2, "GBP", "B"))
      val userAnswers = emptyUserAnswers.set(UnquotedSharesQuery, entries).success.value

      val result = service.removeAssetEntry[SharesEntry](userAnswers, 0, TypeOfAsset.UnquotedShares)

      result.isSuccess mustBe true

      val updated = result.get
      updated.get(UnquotedSharesQuery).value mustBe List(SharesEntry("Two", 2, "GBP", "B"))
    }

    "must return Failure if setting updated answers fails" in {
      val userAnswers = emptyUserAnswers

      val result = service.removeAssetEntry[SharesEntry](userAnswers, 0, TypeOfAsset.UnquotedShares)

      result.isFailure mustBe true
    }
  }

  "getNextAssetRoute" - {

    "must return the first uncompleted journey in order" in {
      val values      = Set[TypeOfAsset](TypeOfAsset.UnquotedShares, TypeOfAsset.QuotedShares)
      val userAnswers = emptyUserAnswers
        .set(TypeOfAssetPage, values).success.value

      val result = service.getNextAssetRoute(userAnswers)

      result mustBe Some(controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesStartController.onPageLoad())
    }

    "must skip journeys not in the selected assets" in {
      val values      = Set[TypeOfAsset](TypeOfAsset.QuotedShares)
      val userAnswers = emptyUserAnswers
        .set(TypeOfAssetPage, values).success.value

      val result = service.getNextAssetRoute(userAnswers)

      result mustBe Some(controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesStartController.onPageLoad())
    }

    "must return None if all selected journeys are completed" in {
      val asset       = TypeOfAsset.UnquotedShares
      val values      = Set[TypeOfAsset](asset)
      val userAnswers = emptyUserAnswers
        .set(TypeOfAssetPage, values).success.value
        .set(AssetCompletionFlag(asset), true).success.value

      val result = service.getNextAssetRoute(userAnswers)

      result mustBe None
    }

    "must return None if no asset types have been selected" in {
      val userAnswers = emptyUserAnswers

      val result = service.getNextAssetRoute(userAnswers)

      result mustBe None
    }
  }

  "setAssetCompleted" - {

    "must return Some(updatedAnswers) if setting and persisting succeeds" in {
      val asset = TypeOfAsset.UnquotedShares

      when(stubSessionRepository.set(any())) thenReturn Future.successful(true)

      val result = await(service.setAssetCompleted(emptyUserAnswers, asset, completed = true))

      result mustBe defined
      result.get.get(AssetCompletionFlag(asset)) mustBe Some(true)

      val resultFalse = await(service.setAssetCompleted(emptyUserAnswers, asset, completed = false))
      resultFalse.get.get(AssetCompletionFlag(asset)) mustBe Some(false)
    }

    "must return None if sessionRepository.set returns false" in {
      val asset = TypeOfAsset.UnquotedShares

      when(stubSessionRepository.set(any())) thenReturn Future.successful(false)

      val result = await(service.setAssetCompleted(emptyUserAnswers, asset, completed = true))

      result mustBe None
    }

  }

}
