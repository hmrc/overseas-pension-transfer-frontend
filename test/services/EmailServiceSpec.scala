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
import config.FrontendAppConfig
import connectors.EmailConnector
import models.email.{EmailAccepted, EmailToSendRequest, SubmissionConfirmation}
import models.{IndividualDetails, MinimalDetails}
import org.mockito.ArgumentMatchers.{any, refEq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import pages.memberDetails.MemberNamePage
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import queries.{DateSubmittedQuery, QtNumberQuery}
import uk.gov.hmrc.http.HeaderCarrier
import utils.DateTimeFormats.{display24h, emailDisplayDate, emailDisplayTime}

import java.time.{Instant, LocalDateTime, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class EmailServiceSpec extends AnyFreeSpec with SpecBase with Matchers with MockitoSugar {

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  "EmailService.sendConfirmationEmail" - {

    "must return Left(SessionDataError) when required data is missing from session data" in {

      val mockConnector = mock[EmailConnector]
      val mockConfig    = mock[FrontendAppConfig]

      when(mockConfig.submittedConfirmationTemplateId).thenReturn("submitted_confirmation_template")

      implicit val appConfig: FrontendAppConfig = mockConfig
      val service                               = new EmailService(mockConnector)

      val minimalDetails = mock[MinimalDetails]
      when(minimalDetails.email).thenReturn("test@example.com")
      when(minimalDetails.organisationName).thenReturn(None)
      when(minimalDetails.individualDetails).thenReturn(Some(IndividualDetails(firstName = "Test", middleName = None, lastName = "User")))

      val result = await(service.sendConfirmationEmail(emptySessionData, minimalDetails))

      result mustBe Left(SessionDataError)
    }

    "must send email and return Right(EmailSentSuccess) when enabled and connector returns EmailAccepted" in {

      val mockConnector = mock[EmailConnector]
      val mockConfig    = mock[FrontendAppConfig]

      when(mockConfig.submittedConfirmationTemplateId).thenReturn("submitted_confirmation_template")

      implicit val appConfig: FrontendAppConfig = mockConfig
      val service                               = new EmailService(mockConnector)

      val emailAddress = "test@example.com"

      val minimalDetails = mock[MinimalDetails]
      when(minimalDetails.email).thenReturn(emailAddress)
      when(minimalDetails.organisationName).thenReturn(None)
      when(minimalDetails.individualDetails).thenReturn(Some(IndividualDetails(firstName = "David", middleName = None, lastName = "Frost")))

      val submittedAt: Instant = Instant.parse("2025-10-01T09:13:00Z")

      val sessionData =
        emptySessionData
          .set(QtNumberQuery, testQtNumber).success.value
          .set(MemberNamePage, testMemberName).success.value
          .set(DateSubmittedQuery, submittedAt).success.value

      val date                         = LocalDateTime.ofInstant(submittedAt, ZoneId.systemDefault()).format(emailDisplayDate)
      val time                         = LocalDateTime.ofInstant(submittedAt, ZoneId.systemDefault()).format(display24h)
      val expectedFormattedSubmittedAt = s"$date at ${time}am"

      val expectedRequest =
        EmailToSendRequest(
          to         = List(emailAddress),
          templateId = "submitted_confirmation_template",
          parameters = SubmissionConfirmation(
            qtReference       = testQtNumber.value,
            memberName        = testMemberName.fullName,
            submitter         = individualSubmitterDetails.fullName,
            submissionDate    = expectedFormattedSubmittedAt,
            pensionSchemeName = sessionData.schemeInformation.schemeName
          )
        )

      when(mockConnector.send(any())(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(EmailAccepted))

      val result = await(service.sendConfirmationEmail(sessionData, minimalDetails))

      result mustBe Right(EmailSentSuccess)
      verify(mockConnector).send(refEq(expectedRequest))(any[ExecutionContext], any[HeaderCarrier])
    }

    "must return Left(DownstreamError) when enabled and connector returns a non-accepted response" in {

      val mockConnector = mock[EmailConnector]
      val mockConfig    = mock[FrontendAppConfig]

      when(mockConfig.submittedConfirmationTemplateId).thenReturn("submitted_confirmation_template")

      implicit val appConfig: FrontendAppConfig = mockConfig
      val service                               = new EmailService(mockConnector)

      val minimalDetails = mock[MinimalDetails]
      when(minimalDetails.email).thenReturn("test@example.com")
      when(minimalDetails.organisationName).thenReturn(None)
      when(minimalDetails.individualDetails).thenReturn(Some(IndividualDetails(firstName = "Test", middleName = None, lastName = "User")))

      val submittedAt: Instant = Instant.parse("2025-10-01T09:13:00Z")

      val sessionData =
        emptySessionData
          .set(QtNumberQuery, testQtNumber).success.value
          .set(MemberNamePage, testMemberName).success.value
          .set(DateSubmittedQuery, submittedAt).success.value

      val nonAccepted = mock[models.email.EmailSendingResult]

      when(mockConnector.send(any())(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(nonAccepted))

      val result = await(service.sendConfirmationEmail(sessionData, minimalDetails))

      result.isLeft mustBe true
      result.left.toOption.get mustBe DownstreamError(nonAccepted.toString)
    }

    "must format date correctly for AM submission time" in {

      val mockConnector = mock[EmailConnector]
      val mockConfig    = mock[FrontendAppConfig]

      when(mockConfig.submittedConfirmationTemplateId).thenReturn("submitted_confirmation_template")

      implicit val appConfig: FrontendAppConfig = mockConfig
      val service                               = new EmailService(mockConnector)

      val minimalDetails = mock[MinimalDetails]
      when(minimalDetails.email).thenReturn("test@example.com")
      when(minimalDetails.organisationName).thenReturn(None)
      when(minimalDetails.individualDetails).thenReturn(Some(IndividualDetails(firstName = "David", middleName = None, lastName = "Frost")))
      when(mockConnector.send(any())(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(EmailAccepted))

      val submittedAtAM: Instant = Instant.parse("2025-10-01T09:08:00Z")
      val sessionDataAM          =
        emptySessionData
          .set(QtNumberQuery, testQtNumber).success.value
          .set(MemberNamePage, testMemberName).success.value
          .set(DateSubmittedQuery, submittedAtAM).success.value

      val date =
        LocalDateTime.ofInstant(submittedAtAM, ZoneId.systemDefault())
          .format(emailDisplayDate)

      val time =
        LocalDateTime.ofInstant(submittedAtAM, ZoneId.systemDefault())
          .format(emailDisplayTime)
          .toLowerCase

      val formatted = s"$date at $time"

      val expectedRequest =
        EmailToSendRequest(
          to         = List("test@example.com"),
          templateId = "submitted_confirmation_template",
          parameters = SubmissionConfirmation(
            qtReference       = testQtNumber.value,
            memberName        = testMemberName.fullName,
            submitter         = individualSubmitterDetails.fullName,
            submissionDate    = formatted,
            pensionSchemeName = sessionDataAM.schemeInformation.schemeName
          )
        )

      val result = await(service.sendConfirmationEmail(sessionDataAM, minimalDetails))

      result mustBe Right(EmailSentSuccess)
      verify(mockConnector).send(refEq(expectedRequest))(any[ExecutionContext], any[HeaderCarrier])
    }

    "must format date correctly for PM submission time" in {

      val mockConnector = mock[EmailConnector]
      val mockConfig    = mock[FrontendAppConfig]

      when(mockConfig.submittedConfirmationTemplateId).thenReturn("submitted_confirmation_template")

      implicit val appConfig: FrontendAppConfig = mockConfig
      val service                               = new EmailService(mockConnector)

      val minimalDetails = mock[MinimalDetails]
      when(minimalDetails.email).thenReturn("test@example.com")
      when(minimalDetails.organisationName).thenReturn(None)
      when(minimalDetails.individualDetails)
        .thenReturn(Some(IndividualDetails("David", None, "Frost")))
      when(mockConnector.send(any())(any[ExecutionContext], any[HeaderCarrier]))
        .thenReturn(Future.successful(EmailAccepted))

      val submittedAtPM: Instant = Instant.parse("2025-10-01T15:45:00Z")
      val sessionDataPM          =
        emptySessionData
          .set(QtNumberQuery, testQtNumber).success.value
          .set(MemberNamePage, testMemberName).success.value
          .set(DateSubmittedQuery, submittedAtPM).success.value

      val date =
        LocalDateTime.ofInstant(submittedAtPM, ZoneId.systemDefault())
          .format(emailDisplayDate)

      val time =
        LocalDateTime.ofInstant(submittedAtPM, ZoneId.systemDefault())
          .format(emailDisplayTime)
          .toLowerCase

      val formatted = s"$date at $time"

      val expectedRequest =
        EmailToSendRequest(
          to         = List("test@example.com"),
          templateId = "submitted_confirmation_template",
          parameters = SubmissionConfirmation(
            qtReference       = testQtNumber.value,
            memberName        = testMemberName.fullName,
            submitter         = individualSubmitterDetails.fullName,
            submissionDate    = formatted,
            pensionSchemeName = sessionDataPM.schemeInformation.schemeName
          )
        )

      val result = await(service.sendConfirmationEmail(sessionDataPM, minimalDetails))

      result mustBe Right(EmailSentSuccess)
      verify(mockConnector).send(refEq(expectedRequest))(any[ExecutionContext], any[HeaderCarrier])
    }
  }
}
