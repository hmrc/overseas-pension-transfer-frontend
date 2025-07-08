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

import connectors.{UserAnswersConnector, UserAnswersErrorResponse, UserAnswersSuccessResponse}
import models.dtos.UserAnswersDTO
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.memberDetails.{MemberDateOfLeavingUKPage, MemberHasEverBeenResidentUKPage, MembersLastUKAddressPage}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MemberDetailsService @Inject() (userAnswersConnector: UserAnswersConnector) {

  // If going from false → true, remove the answers of next questions
  def updateMemberIsResidentUKAnswers(baseAnswers: UserAnswers, previousValue: Option[Boolean], value: Boolean): Future[UserAnswers] = {
    (previousValue, value) match {
      case (Some(false), true) =>
        Future.fromTry(baseAnswers
          .remove(MemberHasEverBeenResidentUKPage)
          .flatMap(_.remove(MembersLastUKAddressPage))
          .flatMap(_.remove(MemberDateOfLeavingUKPage)))
      case _                   =>
        Future.successful(baseAnswers)
    }
  }

  // If going from true → false in CheckMode, switch to NormalMode to question next two questions
  def getMemberIsResidentUKRedirectMode(mode: Mode, previousValue: Option[Boolean], value: Boolean): Mode = {
    (mode, previousValue, value) match {
      case (CheckMode, Some(true), false) => NormalMode
      case _                              => mode
    }
  }

  // If going from false → true, remove the answers of next questions
  def updateMemberHasEverBeenResidentUKAnswers(baseAnswers: UserAnswers, previousValue: Option[Boolean], value: Boolean): Future[UserAnswers] = {
    (previousValue, value) match {
      case (Some(true), false) => Future.fromTry(baseAnswers
          .remove(MembersLastUKAddressPage)
          .flatMap(_.remove(MemberDateOfLeavingUKPage)))
      case _                   => Future.successful(baseAnswers)
    }
  }

  // If going from true → false in CheckMode, switch to NormalMode to question next two questions
  def getMemberHasEverBeenResidentUKRedirectMode(mode: Mode, previousValue: Option[Boolean], value: Boolean): Mode = {
    (mode, previousValue, value) match {
      case (CheckMode, Some(false), true) => NormalMode
      case _                              => mode
    }
  }

  // TODO: This should probably return either user answers or an error
  def postMemberNinoUserAnswers(id: String, userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswers] = {
    for {
      backendResponse <- userAnswersConnector.putAnswers(id, UserAnswersDTO.fromUserAnswers(userAnswers))(hc, ec)
      updatedAnswers   = backendResponse match {
                           case UserAnswersSuccessResponse(updatedUserAnswersDTO) =>
                             UserAnswersDTO.toUserAnswers(updatedUserAnswersDTO)
                           case UserAnswersErrorResponse(_)                       =>
                             userAnswers
                         }
    } yield updatedAnswers
  }

  def postMemberCurrentAddressUserAnswers(id: String, userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswers] = {
    for {
      backendResponse <- userAnswersConnector.putAnswers(id, UserAnswersDTO.fromUserAnswers(userAnswers))(hc, ec)
      updatedAnswers   = backendResponse match {
                           case UserAnswersSuccessResponse(updatedUserAnswersDTO) =>
                             UserAnswersDTO.toUserAnswers(updatedUserAnswersDTO)
                           case UserAnswersErrorResponse(_)                       =>
                             userAnswers
                         }
    } yield updatedAnswers
  }

  // TODO: This should probably return either user answers or an error
  def postMemberDateOfLeavingUKUserAnswers(id: String, userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswers] = {
    for {
      backendResponse <- userAnswersConnector.putAnswers(id, UserAnswersDTO.fromUserAnswers(userAnswers))(hc, ec)
      updatedAnswers   = backendResponse match {
                           case UserAnswersSuccessResponse(updatedUserAnswersDTO) =>
                             UserAnswersDTO.toUserAnswers(updatedUserAnswersDTO)
                           case UserAnswersErrorResponse(_)                       =>
                             userAnswers
                         }
    } yield updatedAnswers
  }
}
