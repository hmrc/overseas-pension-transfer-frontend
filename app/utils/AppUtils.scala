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

package utils

import models.UserAnswers
import pages.MemberNamePage
import play.api.i18n.Messages
import queries.QtNumber

trait AppUtils {

  def memberFullName(userAnswers: UserAnswers): String = {
    userAnswers.get(MemberNamePage).map(_.fullName)
      .getOrElse("Undefined name")
  }

  def qtNumber(userAnswers: UserAnswers): String = {
    userAnswers.get(QtNumber)
      .getOrElse("Undefined QT Number")
  }
}
