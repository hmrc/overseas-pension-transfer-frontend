/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the \"License\");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an \"AS IS\" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import config.FrontendAppConfig
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HttpReads.Implicits.readJsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import java.net.URL
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait UserAnswersResponse

case class UserAnswersSuccessResponse(json: JsValue)  extends UserAnswersResponse
case class UserAnswersErrorResponse(cause: Throwable) extends UserAnswersResponse

class UserAnswersConnector @Inject() (
    appConfig: FrontendAppConfig,
    http: HttpClientV2
  ) extends Logging {

  private def userAnswersUrl(id: String): URL =
    url"${appConfig.backendService}/user-answers/$id"

  def getAnswers(id: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswersResponse] = {
    http.get(userAnswersUrl(id))
      .execute[JsValue]
      .map { json =>
        UserAnswersSuccessResponse(json)
      }
      .recover {
        case e: Exception =>
          logger.warn(s"Error retrieving user answers for ID '$id': ${e.getMessage}", e)
          UserAnswersErrorResponse(e)
      }
  }

  def putAnswers(id: String, body: JsValue)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[UserAnswersResponse] = {
    http.put(userAnswersUrl(id))
      .withBody(body)
      .execute[JsValue]
      .map { updatedJson =>
        UserAnswersSuccessResponse(updatedJson)
      }
      .recover {
        case e: Exception =>
          logger.warn(s"Error updating user answers for ID '$id': ${e.getMessage}", e)
          UserAnswersErrorResponse(e)
      }
  }
}
