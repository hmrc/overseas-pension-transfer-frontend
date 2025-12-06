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
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoreAssetCompletionService @Inject() (
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
      enrichedAnswers = AssetThresholdHandler.handle(userAnswers, assetType, userSelection)

      // Step 4: persist model SaveForLater + enriched full copy Session
      _ <- userAnswersService.setExternalUserAnswers(AssetThresholdHandler.handle(enrichedAnswers, assetType, userSelection))

    } yield updatedSession
  }
}
