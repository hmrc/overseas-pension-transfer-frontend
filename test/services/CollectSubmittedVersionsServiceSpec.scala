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

import base.SpecBase
import connectors.UserAnswersConnector
import models.{PstrNumber, UserAnswers}
import models.QtStatus.Submitted
import models.dtos.UserAnswersDTO
import models.responses.UserAnswersErrorResponse
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CollectSubmittedVersionsServiceSpec extends AnyFreeSpec with SpecBase {

  implicit val hc: HeaderCarrier             = HeaderCarrier()
  private val instant: Instant               = Instant.now
  private val userAnswersDTO: UserAnswersDTO = UserAnswersDTO("id", pstr, JsObject(Map("field" -> JsString("value"))), instant)
  private val userAnswers                    = UserAnswers("id", pstr, JsObject(Map("field" -> JsString("value"))), instant)

  private val mockUserAnswersConnector = mock[UserAnswersConnector]

  protected val service = new CollectSubmittedVersionsService(mockUserAnswersConnector)

  "collectVersions" - {
    "Return a List of one UserAnswer for versionNumber 001" in {

      when(mockUserAnswersConnector.getAnswers(any())(any(), any())).thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))
      when(mockUserAnswersConnector.getAnswers(any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(Future.successful(Right(userAnswersDTO)))

      val result = await(service.collectVersions("QT123456", PstrNumber("12345678AB"), Submitted, "001"))

      result mustBe (None, List(userAnswers))
    }
  }

  "Return a list of one Draft record and one from UserAnswer for versionNumber 001" in {

    when(mockUserAnswersConnector.getAnswers(any())(any(), any())).thenReturn(Future.successful(Right(userAnswersDTO.copy(referenceId = "Draft"))))
    when(mockUserAnswersConnector.getAnswers(any(), any(), any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(Right(userAnswersDTO)))

    val result = await(service.collectVersions("QT123456", PstrNumber("12345678AB"), Submitted, "001"))

    result mustBe (Some(userAnswers.copy(id = "Draft")), List(
      userAnswers
    ))
  }

  "Return a list of one Draft and multiple UserAnswers for versionNumber 003" in {
    when(mockUserAnswersConnector.getAnswers(any())(any(), any())).thenReturn(Future.successful(Right(userAnswersDTO.copy(referenceId = "Draft"))))
    when(mockUserAnswersConnector.getAnswers(any(), any(), any(), any(), any())(any(), any()))
      .thenReturn(Future.successful(Right(userAnswersDTO)))

    val result = await(service.collectVersions("QT123456", PstrNumber("12345678AB"), Submitted, "003"))

    result mustBe (Some(userAnswers.copy(id = "Draft")), List(
      userAnswers,
      userAnswers,
      userAnswers
    ))
  }

  List("010", "100") foreach {
    case version =>
      s"Return a list of length ${version.toInt} when versionNumber = $version" in {
        when(mockUserAnswersConnector.getAnswers(any())(any(), any())).thenReturn(Future.successful(Left(UserAnswersErrorResponse("Error", None))))
        when(mockUserAnswersConnector.getAnswers(any(), any(), any(), any(), any())(any(), any()))
          .thenReturn(Future.successful(Right(userAnswersDTO)))

        val result = await(service.collectVersions("QT123456", PstrNumber("12345678AB"), Submitted, version))

        result._2.length mustBe version.toInt
      }
  }

}
