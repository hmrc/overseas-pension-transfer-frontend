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

package pages.memberDetails

import controllers.memberDetails.routes
import models.address.AddressLookupResult
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object MembersLastUkAddressLookupPage extends QuestionPage[AddressLookupResult] {

  override def path: JsPath = JsPath \ TaskCategory.MemberDetails.toString \ toString

  override def toString: String = "membersLastUkAddressLookup"

  private def nextPage(mode: Mode): Call =
    routes.MembersLastUkAddressSelectController.onPageLoad(mode)

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    nextPage(mode = NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    nextPage(mode = CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    nextPage(mode = FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    nextPage(mode = AmendCheckMode)

  def nextPageNoResults(mode: Mode): Call =
    routes.MembersLastUkAddressNotFoundController.onPageLoad(mode)

  val recoveryModeReturnUrl: String = routes.MembersLastUKAddressController.onPageLoad(NormalMode).url
}
