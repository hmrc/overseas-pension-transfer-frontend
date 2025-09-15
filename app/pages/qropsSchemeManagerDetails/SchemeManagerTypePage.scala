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
import models.{CheckMode, Mode, NormalMode, SchemeManagerType, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object SchemeManagerTypePage extends QuestionPage[SchemeManagerType] {

  override def path: JsPath = JsPath \ TaskCategory.SchemeManagerDetails.toString \ toString

  override def toString: String = "schemeManagerType"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    answers.get(SchemeManagerTypePage) match {
      case Some(SchemeManagerType.Individual)   => routes.SchemeManagersNameController.onPageLoad(NormalMode)
      case Some(SchemeManagerType.Organisation) => routes.SchemeManagerOrganisationNameController.onPageLoad(NormalMode)
      case _                                    => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    answers.get(SchemeManagerTypePage) match {
      case Some(SchemeManagerType.Individual)   => routes.SchemeManagersNameController.onPageLoad(CheckMode)
      case Some(SchemeManagerType.Organisation) => routes.SchemeManagerOrganisationNameController.onPageLoad(CheckMode)
      case _                                    => controllers.routes.JourneyRecoveryController.onPageLoad()
    }

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
