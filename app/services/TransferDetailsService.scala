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

import models.assets.{AssetsMiniJourney, AssetsMiniJourneyRegistry}
import models.{AssetEntry, TypeOfAsset, UserAnswers}
import play.api.libs.json._
import play.api.mvc.Call
import queries.assets._
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class TransferDetailsService @Inject() (
    sessionRepository: SessionRepository
  ) {

  private def assetEntries[A <: AssetEntry: Reads](journey: AssetsMiniJourney[A], userAnswers: UserAnswers): List[A] =
    userAnswers.get(journey.query).getOrElse(Nil)

  def assetCount[A <: AssetEntry: Reads](journey: AssetsMiniJourney[A], userAnswers: UserAnswers): Int = {
    assetEntries(journey, userAnswers).size
  }

  def removeAssetEntry[A <: AssetEntry: Format](
      journey: AssetsMiniJourney[A],
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

  def getNextAssetRoute(userAnswers: UserAnswers): Option[Call] = {
    AssetsMiniJourneyRegistry.firstIncompleteJourney(userAnswers).map(_.call)
  }

  def getAssetEntryAtIndex[A <: AssetEntry: Reads](journey: AssetsMiniJourney[A], userAnswers: UserAnswers, index: Int): Option[A] = {
    assetEntries(journey, userAnswers).lift(index)
  }

  def setAssetCompleted(userAnswers: UserAnswers, assetType: TypeOfAsset, completed: Boolean)(implicit ec: ExecutionContext): Future[Option[UserAnswers]] =
    userAnswers.set(AssetCompletionFlag(assetType), completed) match {
      case Success(updated) =>
        sessionRepository.set(updated).map {
          case true  => Some(updated)
          case false => None
        }
      case Failure(_)       =>
        Future.successful(None)
    }
}
