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

package connectors.parsers

import models.PensionSchemeData
import models.responses.{PensionSchemeError, PensionSchemeNotAssociated}
import play.api.Logging
import play.api.http.Status.{NOT_FOUND, OK}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import utils.DownstreamLogging

object PensionSchemeParser {
  type PensionSchemeDetailsType = Either[PensionSchemeError, PensionSchemeData]

  implicit object GetPensionSchemeDetailsHttpReads extends HttpReads[PensionSchemeDetailsType] with Logging with DownstreamLogging {

    override def read(method: String, url: String, response: HttpResponse): PensionSchemeDetailsType =
      response.status match {
        case OK         => Right(new PensionSchemeData(""))
        case NOT_FOUND  => Left(new PensionSchemeNotAssociated)
        case statusCode => Left(new PensionSchemeNotAssociated)
      }
  }
}
