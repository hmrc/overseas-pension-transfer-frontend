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

import models.{SessionData, UserAnswers}
import models.assets.TypeOfAsset.Cash
import models.assets._
import play.api.libs.json._
import queries.assets._

import scala.util.{Failure, Success, Try}

object AssetsMiniJourneyService {

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

  def removeAssetEntry[A <: AssetEntry: Format](
      journey: RepeatingAssetsMiniJourney[A],
      sessionData: SessionData,
      index: Int
    ): Try[SessionData] = {
    val queryKey = journey.query

    sessionData.get(queryKey) match {
      case Some(currentList) if index >= 0 && index < currentList.size =>
        val updatedList = currentList.patch(index, Nil, 1)
        sessionData.set(queryKey, updatedList)

      case Some(_) =>
        Failure(new IndexOutOfBoundsException(s"Index $index out of bounds"))

      case None =>
        Failure(new NoSuchElementException(s"No entry found at query path ${queryKey.path}"))
    }
  }

  def removeAllAssetEntriesExceptCash(userAnswers: UserAnswers): Try[UserAnswers] = {
    val journeysWithoutCash = AssetsMiniJourneyRegistry.all.filterNot(_.assetType == Cash)

    val clearedData =
      journeysWithoutCash.foldLeft(Try(userAnswers)) {
        case (acc, assetMiniJ) =>
          acc.flatMap { ua =>
            AssetsMiniJourneyRegistry.forType(assetMiniJ.assetType) match {
              case Some(r: RepeatingAssetsMiniJourney[_]) => ua.remove(r.query)
              case Some(s: SingleAssetsMiniJourney[_])    => ua.remove(s.query)
              case _                                      => Success(ua)
            }
          }
      }

    for {
      ua1 <- clearedData
      ua2 <- ua1.set(SelectedAssetTypes, Seq[TypeOfAsset](Cash))
    } yield ua2
  }

  // ----- Single-asset helpers (Cash) -----

  def getSingle[A <: AssetEntry: Reads](
      journey: SingleAssetsMiniJourney[A],
      sessionData: SessionData
    ): Option[A] =
    sessionData.get(journey.query)

  def setSingle[A <: AssetEntry: Writes](
      journey: SingleAssetsMiniJourney[A],
      sessionData: SessionData,
      value: A
    ): Try[SessionData] =
    sessionData.set(journey.query, value)

  def removeSingle[A <: AssetEntry](
      journey: SingleAssetsMiniJourney[A],
      sessionData: SessionData
    ): Try[SessionData] =
    sessionData.remove(journey.query)

  // ----- Shared helpers -----

  def setAssetCompleted(sessionData: SessionData, assetType: TypeOfAsset, completed: Boolean): Try[SessionData] =
    sessionData.set(AssetCompletionFlag(assetType), completed)

  def setSelectedAssetsIncomplete(sessionData: SessionData, selectedAssets: Seq[TypeOfAsset]): Try[SessionData] =
    selectedAssets.foldLeft(Try(sessionData)) {
      case (Success(ua), assetType) =>
        setAssetCompleted(sessionData, assetType, completed = false)
    }

  def clearAllAssetCompletionFlags(sessionData: SessionData): Try[SessionData] =
    sessionData.remove(AssetCompletionFlags)
}
