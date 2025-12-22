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

import models.assets.TypeOfAsset.Cash
import models.assets._
import models.{AmendCheckMode, Mode, SessionData, UserAnswers}
import pages.transferDetails.TypeOfAssetPage
import play.api.libs.json._
import queries.TransferDetailsRecordVersionQuery
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
        userAnswers.set(queryKey, updatedList).map { updatedAnswers =>
          cleanupTypeOfAsset(journey, updatedAnswers)
        }

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
      ua2 <- ua1.set(AnswersSelectedAssetTypes, Seq[TypeOfAsset](Cash))
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

  /** Synchronises the selected asset types between SessionData and UserAnswers.
    *
    * Behaviour:
    *   - Preserves existing asset completion status for assets that remain selected.
    *   - Adds any newly selected asset types to the session with isCompleted = false.
    *   - Removes all user answers data for asset types that were previously selected but are now deselected.
    *   - Updates the SelectedAssetTypes entry in UserAnswers to reflect the new selection.
    *   - Updates the TypeOfAssetPage entry in SessionData to reflect the new set of assets and statuses.
    */
  def handleTypeOfAssetStatusUpdate(sd: SessionData, ua: UserAnswers, selectedAssets: Seq[TypeOfAsset], mode: Mode): Try[(SessionData, UserAnswers)] = {
    val previous: Map[TypeOfAsset, SessionAssetTypeWithStatus] =
      sd.get(SelectedAssetTypesWithStatus).getOrElse(Seq.empty).map(a => a.assetType -> a).toMap

    val removed: Set[TypeOfAsset] = previous.keySet.diff(selectedAssets.toSet)

    val updated: Seq[SessionAssetTypeWithStatus] =
      selectedAssets.map { assetType =>
        val completed = isAssetCompleted(assetType, ua)

        previous
          .get(assetType)
          .map(_.copy(isCompleted = completed))
          .getOrElse(SessionAssetTypeWithStatus(assetType, completed))
      }

    val uaCleared: Try[UserAnswers] =
      removed.toList.foldLeft[Try[UserAnswers]](Success(ua)) { (acc, t) =>
        acc.flatMap(u => clearAssetData(u, t))
      }

    def setAnswers(userAnswers: UserAnswers): Try[UserAnswers] =
      if (mode == AmendCheckMode) {
        userAnswers.set(TypeOfAssetPage, selectedAssets) flatMap {
          answers =>
            answers.remove(TransferDetailsRecordVersionQuery)
        }
      } else {
        userAnswers.set(TypeOfAssetPage, selectedAssets)
      }

    for {
      sd  <- sd.set(SelectedAssetTypesWithStatus, updated)
      ua  <- uaCleared
      ua1 <- setAnswers(ua)
    } yield (sd, ua1)
  }

  private def isAssetCompleted(assetType: TypeOfAsset, ua: UserAnswers): Boolean = {
    val td = ua.data \ "transferDetails"

    assetType match {

      case TypeOfAsset.Cash =>
        (td \ "cashValue").asOpt[JsValue].exists {
          case JsNumber(_) => true
          case _           => false
        }

      case TypeOfAsset.UnquotedShares =>
        (td \ "unquotedShares").asOpt[JsArray].exists(_.value.nonEmpty)

      case TypeOfAsset.QuotedShares =>
        (td \ "quotedShares").asOpt[JsArray].exists(_.value.nonEmpty)

      case TypeOfAsset.Property =>
        (td \ "propertyAssets").asOpt[JsArray].exists(_.value.nonEmpty)

      case TypeOfAsset.Other =>
        (td \ "otherAssets").asOpt[JsArray].exists(_.value.nonEmpty)

      case _ =>
        false
    }
  }

  private def clearAssetData(ua: UserAnswers, t: TypeOfAsset): Try[UserAnswers] =
    AssetsMiniJourneyRegistry.forType(t) match {
      case Some(s: SingleAssetsMiniJourney[_])    => ua.remove(s.query)
      case Some(r: RepeatingAssetsMiniJourney[_]) => ua.remove(r.query)
      case Some(_: AssetsMiniJourneyBase)         => Success(ua)
      case None                                   => Success(ua)
    }

  def setAssetCompleted(sessionData: SessionData, assetType: TypeOfAsset, completed: Boolean): Try[SessionData] = {
    val selectedAssetsWithStatuses = sessionData.get(SelectedAssetTypesWithStatus).getOrElse(Seq.empty)
    val updated                    = {
      if (completed) {
        SelectedAssetTypesWithStatus.markAsCompleted(selectedAssetsWithStatuses, assetType)
      } else {
        SelectedAssetTypesWithStatus.markAsIncomplete(selectedAssetsWithStatuses, assetType)
      }
    }
    sessionData.set(SelectedAssetTypesWithStatus, updated)
  }

  def clearAllAssetCompletionFlags(sessionData: SessionData): Try[SessionData] =
    sessionData.get(SelectedAssetTypesWithStatus) match {
      case None           =>
        Success(sessionData)
      case Some(Nil)      =>
        sessionData.remove(SelectedAssetTypesWithStatus)
      case Some(existing) =>
        val updated = SelectedAssetTypesWithStatus.markAllIncomplete(existing)
        sessionData.set(SelectedAssetTypesWithStatus, updated)
    }

  private def cleanupTypeOfAsset[A <: AssetEntry: Reads](journey: RepeatingAssetsMiniJourney[A], userAnswers: UserAnswers): UserAnswers = {

    val assetType = journey.assetType
    val remaining = userAnswers.get(journey.query).map(_.size).getOrElse(0)

    if (remaining == 0) {
      val cleanedUserAnswers = userAnswers.remove(journey.query).getOrElse(userAnswers)

      cleanedUserAnswers.get(TypeOfAssetPage) match {
        case Some(list) =>
          cleanedUserAnswers
            .set(TypeOfAssetPage, list.filterNot(_ == assetType))
            .getOrElse(cleanedUserAnswers)

        case None =>
          cleanedUserAnswers
      }
    } else {
      userAnswers
    }
  }

}
