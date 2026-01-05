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

import config.FrontendAppConfig
import connectors.EmailConnector
import models.email.{EmailAccepted, EmailToSendRequest, SubmissionConfirmation}
import models.{MinimalDetails, SessionData}
import pages.memberDetails.MemberNamePage
import play.api.Logging
import queries.{DateSubmittedQuery, QtNumberQuery}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait EmailServiceError

case object SessionDataError            extends EmailServiceError
case class DownstreamError(err: String) extends EmailServiceError

sealed trait EmailSuccess

case object EmailSentSuccess    extends EmailSuccess
case object EmailNotSentSuccess extends EmailSuccess

@Singleton
class EmailService @Inject() (
    emailConnector: EmailConnector
  )(implicit executionContext: ExecutionContext,
    appConfig: FrontendAppConfig
  ) extends Logging {

  def sendConfirmationEmail(
      sessionData: SessionData,
      minimalDetails: MinimalDetails
    )(implicit hc: HeaderCarrier
    ): Future[Either[EmailServiceError, EmailSuccess]] = {
    if (appConfig.submissionEmailEnabled) {
      val emailAddress   = minimalDetails.email
      val schemeName     = sessionData.schemeInformation.schemeName
      val sessionDataGet = (sessionData.get(QtNumberQuery), sessionData.get(MemberNamePage), sessionData.get(DateSubmittedQuery))
      sessionDataGet match {
        case (Some(qtRef), Some(memberName), Some(dateSubmitted)) =>
          val emailParameters = {
            SubmissionConfirmation(
              qtRef.value,
              memberName.fullName,
              format(dateSubmitted),
              schemeName
            )
          }
          emailConnector.send(
            EmailToSendRequest(
              List(emailAddress),
              appConfig.submittedConfirmationTemplateId,
              emailParameters
            )
          ) flatMap {
            case EmailAccepted => Future.successful(Right(EmailSentSuccess))
            case err           =>
              logger.warn(s"[EmailService][sendConfirmationEmail] Email not sent due to downstream error: ${err.toString}")
              Future.successful(Left(DownstreamError(err.toString)))
          }
        case _                                                    =>
          logger.warn("[EmailService][sendConfirmationEmail] Email not sent due to missing data in sd")
          Future.successful(Left(SessionDataError))
      }
    } else {
      Future.successful(Right(EmailNotSentSuccess))
    }
  }

  private def format(instant: Instant): String = {
    val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    val time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))
    s"$date at ${time}pm"
  }
}
