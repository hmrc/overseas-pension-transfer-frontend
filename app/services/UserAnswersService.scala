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

import com.google.inject.Inject
import connectors.UserAnswersConnector
import models.UserAnswers
import models.dtos.UserAnswersDTO.toUserAnswers
import models.responses.UserAnswersSuccessResponse
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersService @Inject() (connector: UserAnswersConnector)(implicit ec: ExecutionContext) {

  def getUserAnswers(id: String)(implicit hc: HeaderCarrier): Future[UserAnswers] = {
    connector.getAnswers(id) map {
      case UserAnswersSuccessResponse(userAnswersDTO) => toUserAnswers(userAnswersDTO)
      case _                                          => UserAnswers(id)
    }
  }
}
