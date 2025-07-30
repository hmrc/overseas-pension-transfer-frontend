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

import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes._
import models.{AssetEntry, AssetMiniJourney, HasAssetQuery, TypeOfAsset, UserAnswers}
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

  def assetCount[A <: AssetEntry: Reads: HasAssetQuery](userAnswers: UserAnswers): Int = {
    val queryKey = implicitly[HasAssetQuery[A]].query
    userAnswers.get(queryKey).getOrElse(Nil).size
  }

  private def getQueryKey[A <: AssetEntry: HasAssetQuery]: AssetQuery[List[A]] =
    implicitly[HasAssetQuery[A]].query

  def removeAssetEntry[A <: AssetEntry: Format: HasAssetQuery](
      userAnswers: UserAnswers,
      index: Int
    ): Try[UserAnswers] = {
    val queryKey = implicitly[HasAssetQuery[A]].query

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

  private lazy val orderedAssetsJourneys: Seq[AssetMiniJourney] = Seq(
    AssetMiniJourney(
      TypeOfAsset.UnquotedShares,
      () => UnquotedSharesStartController.onPageLoad(),
      ua => ua.get(AssetCompletionFlag(TypeOfAsset.UnquotedShares)).contains(true)
    ),
    AssetMiniJourney(
      TypeOfAsset.QuotedShares,
      () => QuotedSharesStartController.onPageLoad(),
      ua => ua.get(AssetCompletionFlag(TypeOfAsset.QuotedShares)).contains(true)
    )
  )

  def getNextAssetRoute(userAnswers: UserAnswers): Option[Call] = {
    val selectedAssets: Set[TypeOfAsset] =
      userAnswers.get(SelectedAssetTypes).getOrElse(Set.empty)

    orderedAssetsJourneys
      .filter(journey => selectedAssets.contains(journey.assetType))
      .find(journey => !journey.isCompleted(userAnswers))
      .map(_.call)
  }

  def getAssetEntryAtIndex[A <: AssetEntry: Reads: HasAssetQuery](userAnswers: UserAnswers, index: Int): Option[A] = {
    val assetEntries: Option[List[A]] = userAnswers.get(getQueryKey[A])
    assetEntries match {
      case Some(list) => Some(list(index))
      case _          => None
    }
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
