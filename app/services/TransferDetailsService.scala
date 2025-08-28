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

import models.UserAnswers
import models.assets.{AssetEntry, RepeatingAssetsMiniJourney, SingleAssetsMiniJourney, TypeOfAsset}
import play.api.libs.json._
import queries.assets._

import scala.util.{Failure, Success, Try}

object TransferDetailsService {

  // ----- Repeating-only helpers -----

  private def assetEntries[A <: AssetEntry: Reads](
      journey: RepeatingAssetsMiniJourney[A],
      userAnswers: UserAnswers
    ): List[A] =
    userAnswers.get(journey.query).getOrElse(Nil)

  def assetCount[A <: AssetEntry: Reads](journey: RepeatingAssetsMiniJourney[A], userAnswers: UserAnswers): Int = {
    assetEntries(journey, userAnswers).size
  }

  def getAssetEntryAtIndex[A <: AssetEntry: Reads](journey: RepeatingAssetsMiniJourney[A], userAnswers: UserAnswers, index: Int): Option[A] = {
    assetEntries(journey, userAnswers).lift(index)
  }

  def removeAssetEntry[A <: AssetEntry: Format](
      journey: RepeatingAssetsMiniJourney[A],
      userAnswers: UserAnswers,
      index: Int
    ): Try[UserAnswers] = {
    val queryKey = journey.query

    userAnswers.get(queryKey) match {
      case Some(currentList) if index >= 0 && index < currentList.size =>
        val updatedList = currentList.patch(index, Nil, 1)
        userAnswers.set(queryKey, updatedList)

      case Some(_) =>
        Failure(new IndexOutOfBoundsException(s"Index $index out of bounds"))

      case None =>
        Failure(new NoSuchElementException(s"No entry found at query path ${queryKey.path}"))
    }
  }

  // ----- Single-asset helpers (Cash) -----

  def getSingle[A <: AssetEntry: Reads](
      journey: SingleAssetsMiniJourney[A],
      userAnswers: UserAnswers
    ): Option[A] =
    userAnswers.get(journey.query)

  def setSingle[A <: AssetEntry: Writes](
      journey: SingleAssetsMiniJourney[A],
      userAnswers: UserAnswers,
      value: A
    ): Try[UserAnswers] =
    userAnswers.set(journey.query, value)

  def removeSingle[A <: AssetEntry](
      journey: SingleAssetsMiniJourney[A],
      userAnswers: UserAnswers
    ): Try[UserAnswers] =
    userAnswers.remove(journey.query)

  // ----- Shared helpers -----

  def setAssetCompleted(userAnswers: UserAnswers, assetType: TypeOfAsset, completed: Boolean): Try[UserAnswers] =
    userAnswers.set(AssetCompletionFlag(assetType), completed)

  def setSelectedAssetsIncomplete(ua: UserAnswers, selectedAssets: Set[TypeOfAsset]): Try[UserAnswers] =
    selectedAssets.foldLeft(Try(ua)) {
      case (Success(ua), assetType) =>
        setAssetCompleted(ua, assetType, completed = false)
    }

  def clearAllAssetCompletionFlags(userAnswers: UserAnswers): Try[UserAnswers] =
    userAnswers.remove(AssetCompletionFlags)
}
