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
import queries.assets.{AnswersSelectedAssetTypes, SelectedAssetTypesWithStatus, SessionAssetTypeWithStatus}

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

    "must set isCompleted = true for the given asset" in {
      val sd0 = emptySessionData
        .set(
          SelectedAssetTypesWithStatus,
          SelectedAssetTypesWithStatus.fromTypes(Seq(UnquotedSharesMiniJourney.assetType))
        ).success.value

      val result = service.setAssetCompleted(sd0, UnquotedSharesMiniJourney.assetType, completed = true)

      result mustBe a[Success[_]]
      val updated = result.success.value.get(SelectedAssetTypesWithStatus).value
      updated must contain only SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true)
    }

    "must set isCompleted = false for the given asset" in {
      val sd0 = emptySessionData
        .set(
          SelectedAssetTypesWithStatus,
          Seq(SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted = true))
        ).success.value

      val result = service.setAssetCompleted(sd0, QuotedSharesMiniJourney.assetType, completed = false)

      result mustBe a[Success[_]]
      val updated = result.success.value.get(SelectedAssetTypesWithStatus).value
      updated must contain only SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
    }
  }

  "clearAllAssetCompletionFlags" - {

    "must mark all selected assets as isCompleted = false" in {
      val sdWithCompleted = emptySessionData
        .set(
          SelectedAssetTypesWithStatus,
          Seq(
            SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType, isCompleted = true),
            SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType, isCompleted   = true)
          )
        ).success.value

      val result = service.clearAllAssetCompletionFlags(sdWithCompleted)

      result mustBe a[Success[_]]
      val updated = result.success.value.get(SelectedAssetTypesWithStatus).value
      updated must contain theSameElementsInOrderAs Seq(
        SessionAssetTypeWithStatus(UnquotedSharesMiniJourney.assetType),
        SessionAssetTypeWithStatus(QuotedSharesMiniJourney.assetType)
      )
    }

    "must still succeed (no-op) when there are no selected assets" in {
      val result = service.clearAllAssetCompletionFlags(emptySessionData)

      result mustBe a[Success[_]]
      result.success.value.get(SelectedAssetTypesWithStatus) mustBe None
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
          .set(
            AnswersSelectedAssetTypes,
            Seq[TypeOfAsset](TypeOfAsset.Cash, TypeOfAsset.UnquotedShares, TypeOfAsset.QuotedShares, TypeOfAsset.Other)
          ).success.value

      val result = service.removeAllAssetEntriesExceptCash(uaWithAssetsAndFlags)

      result mustBe a[Success[_]]
      val updated = result.get

      updated.get(UnquotedSharesMiniJourney.query) mustBe None
      updated.get(QuotedSharesMiniJourney.query) mustBe None
      updated.get(OtherAssetsMiniJourney.query) mustBe None

      updated.get(CashMiniJourney.query) mustBe Some(CashEntry(999))

      updated.get(AnswersSelectedAssetTypes) mustBe Some(Seq[TypeOfAsset](TypeOfAsset.Cash))
    }

    "must remove non-cash data even if SelectedAssetTypes already equals Set(Cash)" in {
      val ua =
        emptyUserAnswers
          .set(UnquotedSharesMiniJourney.query, List(UnquotedSharesEntry("Leftover", 10, 1, "C"))).success.value
          .set(AnswersSelectedAssetTypes, Seq[TypeOfAsset](TypeOfAsset.Cash)).success.value

      val result = service.removeAllAssetEntriesExceptCash(ua)

      result mustBe a[Success[_]]
      val updated = result.get

      updated.get(UnquotedSharesMiniJourney.query) mustBe None
      updated.get(AnswersSelectedAssetTypes) mustBe Some(Seq[TypeOfAsset](TypeOfAsset.Cash))
    }

    "must succeed and set SelectedAssetTypes to cash when there is nothing to remove" in {
      val result = service.removeAllAssetEntriesExceptCash(emptyUserAnswers)

      result mustBe a[Success[_]]
      val updated = result.get
      updated.get(AnswersSelectedAssetTypes) mustBe Some(Seq[TypeOfAsset](TypeOfAsset.Cash))
    }
  }
}
