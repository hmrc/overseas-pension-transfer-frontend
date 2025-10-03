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
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) {

  def completeAsset(
      userAnswers: UserAnswers,
      sessionData: SessionData,
      assetType: TypeOfAsset,
      completed: Boolean,
      userSelection: Option[Boolean] = None
    )(implicit hc: HeaderCarrier
    ): Future[SessionData] = {

    for {
      // Step 1: mark asset completed
      updatedSession <- Future.fromTry(
                          AssetsMiniJourneyService.setAssetCompleted(sessionData, assetType, completed)
                        )

      // Step 2 Update Session with Completed Flag
      _              <- sessionRepository.set(updatedSession)

      // Step 3: enrich with threshold flags
      enrichedAnswers = assetThresholdHandler.handle(userAnswers, assetType, userSelection)

      // Step 4: build minimal model for Save For Later
      minimalAnswers = buildMinimal(enrichedAnswers, sessionData, assetType)

      // Step 5: persist minimal model SaveForLater + enriched full copy Session
      _ <- userAnswersService.setExternalUserAnswers(assetThresholdHandler.handle(minimalAnswers, assetType, userSelection))

    } yield updatedSession
  }

  /** Choose correct query for the given asset type and build minimal model */
  private def buildMinimal(userAnswers: UserAnswers, sessionData: SessionData, assetType: TypeOfAsset): UserAnswers = {
    assetType match {
      case TypeOfAsset.Property =>
        UserAnswers.buildMinimal(userAnswers, PropertyQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.Other =>
        UserAnswers.buildMinimal(userAnswers, OtherAssetsQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.QuotedShares =>
        UserAnswers.buildMinimal(userAnswers, QuotedSharesQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.UnquotedShares =>
        UserAnswers.buildMinimal(userAnswers, UnquotedSharesQuery)
          .getOrElse(throw new IllegalStateException(s"Could not build minimal user answers for $assetType"))

      case TypeOfAsset.Cash =>
        throw new IllegalArgumentException("Cash assets not supported for threshold handling")
    }
  }
}
