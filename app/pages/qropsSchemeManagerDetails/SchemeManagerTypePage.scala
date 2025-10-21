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

package pages.qropsSchemeManagerDetails

import controllers.qropsSchemeManagerDetails.routes
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, SchemeManagerType, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object SchemeManagerTypePage extends QuestionPage[SchemeManagerType] {

  override def path: JsPath = JsPath \ TaskCategory.SchemeManagerDetails.toString \ toString

  override def toString: String = "schemeManagerType"

  private def nextPageBase(answers: UserAnswers, mode: Mode): Call =
    answers.get(SchemeManagerTypePage) match {
      case Some(SchemeManagerType.Individual)   => routes.SchemeManagersNameController.onPageLoad(mode)
      case Some(SchemeManagerType.Organisation) => routes.SchemeManagerOrganisationNameController.onPageLoad(mode)
      case _                                    => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    nextPageBase(answers, NormalMode)

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    nextPageBase(answers, CheckMode)

  override protected def nextPageFinalCheckMode(answers: UserAnswers): Call =
    nextPageBase(answers, FinalCheckMode)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call =
    nextPageBase(answers, AmendCheckMode)

  final def changeLink(mode: Mode): Call =
    routes.SchemeManagerTypeController.onPageLoad(mode)

  override def cleanup(maybeSchemeManagerType: Option[SchemeManagerType], userAnswers: UserAnswers): Try[UserAnswers] = {
    maybeSchemeManagerType match {
      case Some(SchemeManagerType.Organisation) => userAnswers
          .remove(SchemeManagersNamePage)
      case Some(SchemeManagerType.Individual)   => userAnswers
          .remove(SchemeManagerOrganisationNamePage)
          .flatMap(_.remove(SchemeManagerOrgIndividualNamePage))
      case _                                    => super.cleanup(maybeSchemeManagerType, userAnswers)
    }
  }
}
