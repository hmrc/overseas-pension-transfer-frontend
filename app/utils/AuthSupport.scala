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
import models.authentication._
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

  def extractUserTypeAndPsaPspId(enrolments: Enrolments, config: FrontendAppConfig): (UserType, PsaPspId) = {
    val matchedOpt: Option[(Enrolment, config.EnrolmentConfig, UserType)] =
      enrolments.enrolments.collectFirst {
        case e if e.key == config.psaEnrolment.serviceName => (e, config.psaEnrolment, Psa)
        case e if e.key == config.pspEnrolment.serviceName => (e, config.pspEnrolment, Psp)
      }

    val (enrolment, enrolmentConfig, userType) = getOrElseFailWithUnauthorised(
      matchedOpt,
      "Unable to retrieve matching PSA or PSP enrolment"
    )

    val id = getOrElseFailWithUnauthorised(
      enrolment.getIdentifier(enrolmentConfig.identifierKey).map(_.value),
      s"Unable to retrieve identifier from enrolment ${enrolment.key}"
    )

    userType match {
      case Psa => (Psa, PsaId(id))
      case Psp => (Psp, PspId(id))
    }
  }

  def getOrElseFailWithUnauthorised[T](opt: Option[T], message: String): T = {
    opt.getOrElse {
      logger.warn(message)
      throw new IllegalStateException(message)
    }
  }
}
