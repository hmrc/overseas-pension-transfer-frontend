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
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) {

  def completeAsset(
      userAnswers: UserAnswers,
      assetType: TypeOfAsset,
      completed: Boolean,
      userSelection: Option[Boolean] = None
    )(implicit hc: HeaderCarrier
    ): Future[UserAnswers] = {

    for {
      // Step 1: mark asset completed
      updatedAnswers <- Future.fromTry(
                          AssetsMiniJourneyService.setAssetCompleted(userAnswers, assetType, completed)
                        )

      // Step 2: enrich with threshold flags
      enrichedAnswers = assetThresholdHandler.handle(updatedAnswers, assetType, userSelection)

      // Step 3: build minimal model for Sessiom
      minimalAnswers = buildMinimal(enrichedAnswers, assetType)

      // Step 4: persist minimal model Sessiom + enriched full copy Session
      _ <- userAnswersService.setExternalUserAnswers(assetThresholdHandler.handle(minimalAnswers, assetType, userSelection))

    } yield enrichedAnswers
  }

  /** Choose correct query for the given asset type and build minimal model */
  private def buildMinimal(userAnswers: UserAnswers, assetType: TypeOfAsset): UserAnswers = {
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
