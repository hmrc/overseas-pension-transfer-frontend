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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import models.authentication.{AuthenticatedUser, PsaId, PsaUser, PspUser}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import scala.concurrent.ExecutionContext

class PensionSchemeConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
  )(implicit ec: ExecutionContext) {

  def checkAssociation(srn: String, user: AuthenticatedUser)(implicit hc: HeaderCarrier) = {
    val url        = url"${appConfig.pensionSchemeService}/register-scheme"
    val userHeader = {
      user match {
        case PsaUser(psaId, _) => "psaId" -> psaId.value
        case PspUser(pspId, _) => "pspId" -> pspId.value
      }
    }

    http.post(url)
      .setHeader(
        "schemeReferenceNumber" -> srn,
        userHeader
      )
      .execute[Boolean]
  }

}
