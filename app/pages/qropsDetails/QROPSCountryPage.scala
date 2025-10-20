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

package pages.qropsDetails

import controllers.qropsDetails.routes
import models.address.Country
import models.{Mode, NormalMode, TaskCategory, UserAnswers}
import pages.QuestionPage
import play.api.libs.json.JsPath
import play.api.mvc.Call

import scala.util.Try

case object QROPSCountryPage extends QuestionPage[Country] {

  override def path: JsPath = JsPath \ TaskCategory.QROPSDetails.toString \ toString

  override def toString: String = "qropsEstablished"

  override protected def nextPageNormalMode(answers: UserAnswers): Call = {
    answers.get(QROPSCountryPage) match {
      case Some(Country("ZZ", "Other")) => routes.QROPSOtherCountryController.onPageLoad(NormalMode)
      case Some(Country(_, _))          => routes.QROPSDetailsCYAController.onPageLoad()
      case _                            => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  override protected def nextPageCheckMode(answers: UserAnswers): Call =
    nextPageNormalMode(answers)

  override protected def nextPageAmendCheckMode(answers: UserAnswers): Call = {
    answers.get(QROPSCountryPage) match {
      case Some(Country("ZZ", "Other")) => routes.QROPSOtherCountryController.onPageLoad(NormalMode)
      case Some(Country(_, _))          => controllers.routes.ViewAmendSubmittedController.amend()
      case _                            => controllers.routes.JourneyRecoveryController.onPageLoad()
    }
  }

  final def changeLink(mode: Mode): Call =
    routes.QROPSCountryController.onPageLoad(mode)

  override def cleanup(value: Option[Country], userAnswers: UserAnswers): Try[UserAnswers] = {
    value match {
      case Some(Country("ZZ", "Other")) => super.cleanup(value, userAnswers)
      case _                            => userAnswers.remove(QROPSOtherCountryPage)
    }
  }

  val recoveryModeReturnUrl: String = routes.QROPSCountryController.onPageLoad(NormalMode).url
}
