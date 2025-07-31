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
import models._
import models.assets.{QuotedSharesMiniJourney, UnquotedSharesMiniJourney}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.transferDetails.TypeOfAssetPage
import play.api.test.Helpers._
import queries.assets.AssetCompletionFlag
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

class TransferDetailsServiceSpec extends AnyFreeSpec with SpecBase {

  private val sessionRepository = mock[SessionRepository]

  private val service = new TransferDetailsService(sessionRepository)

  "assetCount" - {
    "must return the number of entries for a given asset journey" in {
      val entries     = List(UnquotedSharesEntry("Foo", 1, "100", "Class A"))
      val userAnswers = emptyUserAnswers.set(UnquotedSharesMiniJourney.query, entries).success.value

      val result = service.assetCount(UnquotedSharesMiniJourney, userAnswers)
      result mustBe 1
    }

    "must return 0 if no entries exist for the asset journey" in {
      val result = service.assetCount(UnquotedSharesMiniJourney, emptyUserAnswers)
      result mustBe 0
    }
  }

  "removeAssetEntry" - {
    "must remove the specified entry and return updated answers" in {
      val entries     = List(UnquotedSharesEntry("One", 1, "100", "A"), UnquotedSharesEntry("Two", 2, "200", "B"))
      val userAnswers = emptyUserAnswers.set(UnquotedSharesMiniJourney.query, entries).success.value

      val result = service.removeAssetEntry(UnquotedSharesMiniJourney, userAnswers, 0)

      result mustBe a[Success[_]]
      val updated = result.get
      updated.get(UnquotedSharesMiniJourney.query).value mustBe List(UnquotedSharesEntry("Two", 2, "200", "B"))
    }

    "must return Failure if the index is out of bounds" in {
      val entries     = List(UnquotedSharesEntry("Only", 1, "100", "X"))
      val userAnswers = emptyUserAnswers.set(UnquotedSharesMiniJourney.query, entries).success.value

      val result = service.removeAssetEntry(UnquotedSharesMiniJourney, userAnswers, 5)

      result mustBe a[Failure[_]]
      result.failed.get mustBe a[IndexOutOfBoundsException]
    }

    "must return Failure if no entries exist at the query path" in {
      val result = service.removeAssetEntry(UnquotedSharesMiniJourney, emptyUserAnswers, 0)

      result mustBe a[Failure[_]]
      result.failed.get mustBe a[NoSuchElementException]
    }
  }

  "getNextAssetRoute" - {
    "must return the first uncompleted journey in order" in {
      val selectedTypes: Set[TypeOfAsset] = Set(UnquotedSharesMiniJourney.assetType, QuotedSharesMiniJourney.assetType)
      val userAnswers                     = emptyUserAnswers.set(TypeOfAssetPage, selectedTypes).success.value

      val result = service.getNextAssetRoute(userAnswers).map(_.toString)
      result mustBe Some(UnquotedSharesMiniJourney.call.url)
    }

    "must skip journeys not in the selected assets" in {
      val selectedTypes: Set[TypeOfAsset] = Set(QuotedSharesMiniJourney.assetType)
      val userAnswers                     = emptyUserAnswers.set(TypeOfAssetPage, selectedTypes).success.value

      val result = service.getNextAssetRoute(userAnswers).map(_.toString)
      result mustBe Some(QuotedSharesMiniJourney.call.url)
    }

    "must return None if all selected journeys are completed" in {
      val userAnswers = emptyUserAnswers
        .set[Set[TypeOfAsset]](TypeOfAssetPage, Set(UnquotedSharesMiniJourney.assetType)).success.value
        .set(AssetCompletionFlag(UnquotedSharesMiniJourney.assetType), true).success.value

      val result = service.getNextAssetRoute(userAnswers)
      result mustBe None
    }

    "must return None if no asset types have been selected" in {
      val result = service.getNextAssetRoute(emptyUserAnswers)
      result mustBe None
    }
  }

  "setAssetCompleted" - {
    "must return Some(updatedAnswers) if setting and persisting succeeds" in {
      when(sessionRepository.set(any())) thenReturn Future.successful(true)

      val result = await(service.setAssetCompleted(emptyUserAnswers, UnquotedSharesMiniJourney.assetType, completed = true))
      result.value.get(AssetCompletionFlag(UnquotedSharesMiniJourney.assetType)) mustBe Some(true)
    }

    "must return None if sessionRepository.set returns false" in {
      when(sessionRepository.set(any())) thenReturn Future.successful(false)

      val result = await(service.setAssetCompleted(emptyUserAnswers, UnquotedSharesMiniJourney.assetType, completed = true))
      result mustBe None
    }
  }
}
