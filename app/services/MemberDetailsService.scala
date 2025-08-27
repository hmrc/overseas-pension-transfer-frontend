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

import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.memberDetails.{MemberDateOfLeavingUKPage, MemberHasEverBeenResidentUKPage, MembersLastUKAddressPage}

import scala.concurrent.Future
import scala.util.{Success, Try}

class MemberDetailsService {

  // If going from false → true, remove the answers of next questions
  def updateMemberIsResidentUKAnswers(baseAnswers: UserAnswers, previousValue: Option[Boolean], value: Boolean): Try[UserAnswers] = {
    (previousValue, value) match {
      case (Some(false), true) =>
        baseAnswers
          .remove(MemberHasEverBeenResidentUKPage)
          .flatMap(_.remove(MembersLastUKAddressPage))
          .flatMap(_.remove(MemberDateOfLeavingUKPage))
      case _                   =>
        Success(baseAnswers)
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
  def updateMemberHasEverBeenResidentUKAnswers(baseAnswers: UserAnswers, previousValue: Option[Boolean], value: Boolean): Try[UserAnswers] = {
    (previousValue, value) match {
      case (Some(true), false) => baseAnswers
          .remove(MembersLastUKAddressPage)
          .flatMap(_.remove(MemberDateOfLeavingUKPage))
      case _                   => Success(baseAnswers)
    }
  }

  // If going from true → false in CheckMode, switch to NormalMode to question next two questions
  def getMemberHasEverBeenResidentUKRedirectMode(mode: Mode, previousValue: Option[Boolean], value: Boolean): Mode = {
    (mode, previousValue, value) match {
      case (CheckMode, Some(false), true) => NormalMode
      case _                              => mode
    }
  }
}
