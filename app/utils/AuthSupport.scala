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

import config.FrontendAppConfig
import play.api.Logging
import uk.gov.hmrc.auth.core.AuthProvider.GovernmentGateway
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate

trait AuthSupport extends Logging {

  def buildPredicate(config: FrontendAppConfig): Predicate = {
    AuthProviders(GovernmentGateway) and
      (Enrolment(config.psaEnrolment.serviceName)
        or Enrolment(config.pspEnrolment.serviceName))
  }

  def extractPsaPspId(enrolments: Enrolments, config: FrontendAppConfig): String = {
    val matchedEnrolmentOpt: Option[(Enrolment, config.EnrolmentConfig)] =
      enrolments.enrolments.collectFirst {
        case psa if psa.key == config.psaEnrolment.serviceName => (psa, config.psaEnrolment)
        case psp if psp.key == config.pspEnrolment.serviceName => (psp, config.pspEnrolment)
      }

    val (enrolment, enrolmentConfig) = getOrElseFailWithUnauthorised(
      matchedEnrolmentOpt,
      "Unable to retrieve matching PSA or PSP enrolment"
    )

    val idOpt = enrolment.getIdentifier(enrolmentConfig.identifierKey).map(_.value)
    getOrElseFailWithUnauthorised(idOpt, s"Unable to retrieve identifier from enrolment ${enrolment.key}")
  }

  def getOrElseFailWithUnauthorised[T](opt: Option[T], message: String): T = {
    opt.getOrElse {
      logger.warn(message)
      throw new IllegalStateException(message)
    }
  }
}
