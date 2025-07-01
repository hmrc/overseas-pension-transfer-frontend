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

import models.{TypeOfAsset, UserAnswers}
import queries.{QuotedShares, UnquotedShares}
import repositories.SessionRepository

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.{Failure, Success}

class TransferDetailsService @Inject() (
    sessionRepository: SessionRepository
  ) {

  def assetCount(userAnswers: UserAnswers, assetType: TypeOfAsset): Int = {
    userAnswers.get(getQueryKey(assetType)).getOrElse(Nil).size
  }

  private def getQueryKey(assetType: TypeOfAsset) = {
    val queryKey = assetType match {
      case TypeOfAsset.UnquotedShares => UnquotedShares
      case TypeOfAsset.QuotedShares   => QuotedShares
      case other                      =>
        throw new UnsupportedOperationException(s"Asset type not supported: $other")
    }
    queryKey
  }

  def doAssetRemoval(userAnswers: UserAnswers, index: Int, assetType: TypeOfAsset): Future[Boolean] = {
    val queryKey          = getQueryKey(assetType)
    val updatedList       = userAnswers.get(queryKey).getOrElse(Nil).patch(index, Nil, 1)
    val updatedAnswersTry = userAnswers.set(queryKey, updatedList)

    updatedAnswersTry match {
      case Success(updatedAnswers) => sessionRepository.set(updatedAnswers)
      case Failure(_)              => Future.successful(false)
    }
  }
}
