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

import connectors.PensionSchemeConnector
import models.authentication.{PsaId, PspId}
import models.responses.{PensionSchemeError, PensionSchemeNotAssociated}
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorisingPsaService @Inject() (
    pensionSchemeConnector: PensionSchemeConnector
  )(implicit ex: ExecutionContext
  ) extends Logging {

  def checkIsAuthorisingPsa(srn: String, psaId: PsaId)(implicit hc: HeaderCarrier): Future[Boolean] =
    pensionSchemeConnector
      .getAuthorisingPsa(srn)
      .map {
        case Right(returnedPsaId) =>
          returnedPsaId == psaId

        case Left(err: PensionSchemeNotAssociated) =>
          logger.warn(s"[AuthService][checkIsAuthorisingPsa] PSA not associated with scheme for this request - $err")
          false

        case Left(err: PensionSchemeError) =>
          logger.warn(s"[AuthService][checkIsAuthorisingPsa] Error while checking authorising PSA for for this request - $err")
          false
      }
}
