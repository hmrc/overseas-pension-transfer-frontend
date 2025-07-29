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
import models.{AssetEntry, AssetMiniJourney, SharesEntry, TypeOfAsset, UserAnswers}
import pages.transferDetails.TypeOfAssetPage
import play.api.libs.json.{JsArray, JsPath, Reads, Writes}
import play.api.mvc.Call
import queries.{Gettable, Settable}
import queries.assets.{AssetCompletionFlag, AssetQuery, QuotedShares, SelectedAssetTypes, UnquotedShares}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TransferDetailsService @Inject() (
    sessionRepository: SessionRepository
  ) {

  def assetCount[A <: AssetEntry: Reads](userAnswers: UserAnswers, assetType: TypeOfAsset): Int = {
    userAnswers.get(getQueryKey[A](assetType)).getOrElse(Nil).size
  }

  // We safely cast here because we know the mapping from TypeOfAsset to AssetQuery is exact.
  private def getQueryKey[A <: AssetEntry](assetType: TypeOfAsset): AssetQuery[List[A]] = {
    assetType match {
      case TypeOfAsset.UnquotedShares => UnquotedShares.asInstanceOf[AssetQuery[List[A]]]
      case TypeOfAsset.QuotedShares   => QuotedShares.asInstanceOf[AssetQuery[List[A]]]
      case other                      =>
        throw new UnsupportedOperationException(s"Asset type not supported: $other")
    }
  }

  def doAssetRemoval[A <: AssetEntry: Reads: Writes](
      userAnswers: UserAnswers,
      index: Int,
      assetType: TypeOfAsset
    )(implicit ec: ExecutionContext
    ): Future[Boolean] = {
    val queryKey          = getQueryKey[A](assetType)
    val updatedList       = userAnswers.get(queryKey).getOrElse(Nil).patch(index, Nil, 1)
    val updatedAnswersTry = userAnswers.set(queryKey, updatedList)

    updatedAnswersTry match {
      case Success(updatedAnswers) =>
        sessionRepository.set(updatedAnswers).map {
          case true  => true
          case false => false
        }
      case Failure(_)              => Future.successful(false)
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

//  private def getAssetAtIndex(userAnswers: UserAnswers, assetType: TypeOfAsset, index: Int): Option[AssetEntry] = {
//    userAnswers.get(getQueryKey(assetType))
//    }
//  }

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
