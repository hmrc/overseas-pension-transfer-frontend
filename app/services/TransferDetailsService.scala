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

import models.{ShareEntry, ShareType, TaskCategory, UserAnswers}
import pages.transferDetails._
import javax.inject.Inject

class TransferDetailsService @Inject() {

  def quotedShareBuilder(answers: UserAnswers): Option[ShareEntry] = {
    for {
      name   <- answers.get(QuotedShareCompanyNamePage)
      value  <- answers.get(QuotedShareValuePage)
      number <- answers.get(NumberOfQuotedSharesPage)
      sClass <- answers.get(ClassOfQuotedSharesPage)
    } yield ShareEntry(name, value, number, sClass, ShareType.Quoted)
  }

  def unquotedShareBuilder(answers: UserAnswers): Option[ShareEntry] = {
    for {
      name   <- answers.get(UnquotedShareCompanyNamePage)
      value  <- answers.get(UnquotedShareValuePage)
      number <- answers.get(NumberOfUnquotedSharesPage)
      sClass <- answers.get(UnquotedSharesClassPage)
    } yield ShareEntry(name, value, number, sClass, ShareType.Unquoted)
  }

  def clearUnquotedShareFields(userAnswers: UserAnswers): UserAnswers = {
    userAnswers
      .remove(UnquotedShareCompanyNamePage)
      .flatMap(_.remove(UnquotedShareValuePage))
      .flatMap(_.remove(NumberOfUnquotedSharesPage))
      .flatMap(_.remove(UnquotedSharesClassPage))
      .getOrElse(userAnswers)
  }
}
