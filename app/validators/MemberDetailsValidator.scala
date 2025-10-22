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

package validators

import cats.implicits._
import models.{DataMissingError, GenericError, PersonName, UserAnswers, ValidationResult}
import models.transferJourneys.MemberDetails
import pages.memberDetails._

object MemberDetailsValidator extends Validator[MemberDetails] {

  //Use for comprehension instead of one-stop-shop bigTuple.mapN
  override def fromUserAnswers(user: UserAnswers): ValidationResult[MemberDetails] = ???


  private def validateMemberName(answers: UserAnswers): ValidationResult[PersonName] =
    answers.get(MemberNamePage) match {
      case Some(name) => name.validNec
      case None => DataMissingError(MemberNamePage).invalidNec
    }

  private def validateMemberNino(answers: UserAnswers): ValidationResult[Option[String]] =
    (answers.get(MemberNinoPage), answers.get(MemberDoesNotHaveNinoPage)) match {
      case (Some(nino), None) => Some(nino).validNec
      case (None, Some(_)) => None.validNec
      case (Some(_), Some(_)) => GenericError("Cannot have valid payload with nino and reasonNoNINO").invalidNec
      case (None, None) => DataMissingError(MemberNinoPage).invalidNec
    }
}
