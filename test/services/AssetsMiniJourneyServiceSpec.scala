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
import models.assets._
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.Json
import queries.assets.{AssetCompletionFlag, AssetCompletionFlags, SelectedAssetTypes}

import scala.util.{Failure, Success}

class AssetsMiniJourneyServiceSpec extends AnyFreeSpec with SpecBase {

  private val service = AssetsMiniJourneyService

  "assetCount" - {
    "must return the number of entries for a given asset journey" in {
      val entries     = List(UnquotedSharesEntry("Foo", 1, 100, "Class A"))
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
      val entries     = List(UnquotedSharesEntry("One", 1, 100, "A"), UnquotedSharesEntry("Two", 2, 200, "B"))
      val userAnswers = emptyUserAnswers.set(UnquotedSharesMiniJourney.query, entries).success.value

      val result = service.removeAssetEntry(UnquotedSharesMiniJourney, userAnswers, 0)

      result mustBe a[Success[_]]
      val updated = result.get
      updated.get(UnquotedSharesMiniJourney.query).value mustBe List(UnquotedSharesEntry("Two", 2, 200, "B"))
    }

    "must return Failure if the index is out of bounds" in {
      val entries     = List(UnquotedSharesEntry("Only", 1, 100, "X"))
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

  "setAssetCompleted" - {
    "must return updated UserAnswers with flag set to true" in {
      val result = service.setAssetCompleted(emptyUserAnswers, UnquotedSharesMiniJourney.assetType, completed = true)

      result mustBe a[Success[_]]
      result.get.get(AssetCompletionFlag(UnquotedSharesMiniJourney.assetType)) mustBe Some(true)
    }

    "must return updated UserAnswers with flag set to false" in {
      val result = service.setAssetCompleted(emptyUserAnswers, QuotedSharesMiniJourney.assetType, completed = false)

      result mustBe a[Success[_]]
      result.get.get(AssetCompletionFlag(QuotedSharesMiniJourney.assetType)) mustBe Some(false)
    }
  }
  "clearAllAssetCompletionFlags" - {
    "must remove all completion flags" in {
      val userAnswersWithFlags = emptyUserAnswers
        .set(AssetCompletionFlag(UnquotedSharesMiniJourney.assetType), true).success.value
        .set(AssetCompletionFlag(QuotedSharesMiniJourney.assetType), true).success.value

      val result = service.clearAllAssetCompletionFlags(userAnswersWithFlags)

      result mustBe a[Success[_]]
      val updatedAnswers = result.get

      updatedAnswers.get(AssetCompletionFlag(UnquotedSharesMiniJourney.assetType)) mustBe None
      updatedAnswers.get(AssetCompletionFlag(QuotedSharesMiniJourney.assetType)) mustBe None
    }

    "must still succeed if there are no flags to remove" in {
      val result = service.clearAllAssetCompletionFlags(emptyUserAnswers)

      result mustBe a[Success[_]]
      result.get.data mustBe Json.obj()
    }
  }
  "removeAllAssetEntriesExceptCash" - {

    "must remove all non-cash asset data, clear completion flags, and set selection to cash only" in {

      val uaWithAssetsAndFlags =
        emptyUserAnswers
          .set(UnquotedSharesMiniJourney.query, List(UnquotedSharesEntry("UQ Co", 10, 1, "A"))).success.value
          .set(QuotedSharesMiniJourney.query, List(QuotedSharesEntry("Q Co", 20, 2, "B"))).success.value
          .set(OtherAssetsMiniJourney.query, List(OtherAssetsEntry("Gold", 30))).success.value
          .set(CashMiniJourney.query, CashEntry(999)).success.value
          .set(SelectedAssetTypes, Seq[TypeOfAsset](TypeOfAsset.Cash, TypeOfAsset.UnquotedShares, TypeOfAsset.QuotedShares, TypeOfAsset.Other)).success.value
          .set(AssetCompletionFlag(TypeOfAsset.UnquotedShares), true).success.value
          .set(AssetCompletionFlag(TypeOfAsset.QuotedShares), true).success.value
          .set(AssetCompletionFlag(TypeOfAsset.Other), true).success.value
          .set(AssetCompletionFlag(TypeOfAsset.Cash), true).success.value

      val result = service.removeAllAssetEntriesExceptCash(uaWithAssetsAndFlags)

      result mustBe a[Success[_]]
      val updated = result.get

      updated.get(UnquotedSharesMiniJourney.query) mustBe None
      updated.get(QuotedSharesMiniJourney.query) mustBe None
      updated.get(OtherAssetsMiniJourney.query) mustBe None

      updated.get(CashMiniJourney.query) mustBe Some(CashEntry(999))

      updated.get(SelectedAssetTypes) mustBe Some(Seq[TypeOfAsset](TypeOfAsset.Cash))

      updated.get(AssetCompletionFlag(TypeOfAsset.UnquotedShares)) mustBe None
      updated.get(AssetCompletionFlag(TypeOfAsset.QuotedShares)) mustBe None
      updated.get(AssetCompletionFlag(TypeOfAsset.Other)) mustBe None
      updated.get(AssetCompletionFlag(TypeOfAsset.Cash)) mustBe None

      updated.get(AssetCompletionFlags) mustBe None
    }

    "must remove non-cash data even if SelectedAssetTypes already equals Set(Cash)" in {
      val ua =
        emptyUserAnswers
          .set(UnquotedSharesMiniJourney.query, List(UnquotedSharesEntry("Leftover", 10, 1, "C"))).success.value
          .set(SelectedAssetTypes, Seq[TypeOfAsset](TypeOfAsset.Cash)).success.value

      val result = service.removeAllAssetEntriesExceptCash(ua)

      result mustBe a[Success[_]]
      val updated = result.get

      updated.get(UnquotedSharesMiniJourney.query) mustBe None
      updated.get(SelectedAssetTypes) mustBe Some(Seq[TypeOfAsset](TypeOfAsset.Cash))
    }

    "must succeed and set SelectedAssetTypes to cash when there is nothing to remove" in {
      val result = service.removeAllAssetEntriesExceptCash(emptyUserAnswers)

      result mustBe a[Success[_]]
      val updated = result.get
      updated.get(SelectedAssetTypes) mustBe Some(Seq[TypeOfAsset](TypeOfAsset.Cash))
    }
  }
}
