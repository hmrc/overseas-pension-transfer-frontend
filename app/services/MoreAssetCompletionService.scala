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

import handlers.AssetThresholdHandler
import models.assets._
import models.{SessionData, UserAnswers}
import queries.assets._
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoreAssetCompletionService @Inject() (
    assetThresholdHandler: AssetThresholdHandler,
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext
  ) {

  def completeAsset(
      sessionData: SessionData,
      assetType: TypeOfAsset,
      completed: Boolean,
      userSelection: Option[Boolean] = None
    ): Future[SessionData] = {

    for {
      // Step 1: mark asset completed
      updatedSessionData <- Future.fromTry(
                              AssetsMiniJourneyService.setAssetCompleted(sessionData, assetType, completed)
                            )

      // Step 2: enrich with threshold flags
      enrichedSessionData = assetThresholdHandler.handle(updatedSessionData, assetType, userSelection)

      // Step 3: build minimal model for Sessiom
      minimalSessionData = buildMinimal(enrichedSessionData, assetType)

      // Step 4: persist minimal model Sessiom + enriched full copy Session
      _ <- sessionRepository.set(assetThresholdHandler.handle(minimalSessionData, assetType, userSelection))

    } yield enrichedSessionData
  }

  /** Choose correct query for the given asset type and build minimal model */
  private def buildMinimal(sessionData: SessionData, assetType: TypeOfAsset): SessionData = {
    assetType match {
      case TypeOfAsset.Property =>
        UserAnswers.buildMinimal(sessionData, PropertyQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.Other =>
        UserAnswers.buildMinimal(sessionData, OtherAssetsQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.QuotedShares =>
        UserAnswers.buildMinimal(sessionData, QuotedSharesQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.UnquotedShares =>
        UserAnswers.buildMinimal(sessionData, UnquotedSharesQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.Cash =>
        throw new IllegalArgumentException("Cash assets not supported for threshold handling")
    }
  }
}
