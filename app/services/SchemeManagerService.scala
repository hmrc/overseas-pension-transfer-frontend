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

import models.{Mode, NormalMode, SchemeManagerType, UserAnswers}
import pages.qropsSchemeManagerDetails.{SchemeManagerOrgIndividualNamePage, SchemeManagerOrganisationNamePage, SchemeManagersNamePage}

import javax.inject.Inject
import scala.concurrent.Future

class SchemeManagerService @Inject() {

  // If the SchemeManagerType changes, remove the answers of previous corresponding questions
  def updateSchemeManagerTypeAnswers(
      answers: UserAnswers,
      previousValue: Option[SchemeManagerType],
      value: SchemeManagerType
    ): Future[UserAnswers] = {
    (previousValue, value) match {
      case (Some(SchemeManagerType.Individual), SchemeManagerType.Organisation) => Future.fromTry(answers
          .remove(SchemeManagersNamePage))
      case (Some(SchemeManagerType.Organisation), SchemeManagerType.Individual) => Future.fromTry(answers
          .remove(SchemeManagerOrganisationNamePage)
          .flatMap(_.remove(SchemeManagerOrgIndividualNamePage)))
      case _                                                                    => Future.successful(answers)
    }
  }

  // If the SchemeManagerType changes, always switch to NormalMode to go through corresponding set of questions
  def getSchemeManagerTypeRedirectMode(mode: Mode, previousValue: Option[SchemeManagerType], value: SchemeManagerType): Mode = {
    if (!previousValue.contains(value)) NormalMode else mode
  }
}
