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
import utils.DateTimeFormats.{display12h, displayDate}

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}
import java.util.Locale
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

sealed trait EmailServiceError

case object MinimalDetailsError         extends EmailServiceError
case object SessionDataError            extends EmailServiceError
case class DownstreamError(err: String) extends EmailServiceError

sealed trait EmailSuccess

case object EmailSentSuccess    extends EmailSuccess
// EmailNotSentSuccess is returned when the submission email is disabled in the config
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
    val emailAddress   = minimalDetails.email
    val schemeName     = sessionData.schemeInformation.schemeName
    val maybeSubmitter = minimalDetails.organisationName.orElse(minimalDetails.individualDetails.map(_.fullName))
    val sessionDataGet = (sessionData.get(QtNumberQuery), sessionData.get(MemberNamePage), sessionData.get(DateSubmittedQuery))
    sessionDataGet match {
      case (Some(qtRef), Some(memberName), Some(dateSubmitted)) =>
        maybeSubmitter match {
          case Some(submitter) =>
            val emailParameters = {
              SubmissionConfirmation(
                qtRef.value,
                memberName.fullName,
                submitter,
                format(dateSubmitted),
                schemeName
              )
            }
            emailConnector.send(EmailToSendRequest(
              List(emailAddress),
              appConfig.submittedConfirmationTemplateId,
              emailParameters
            )) flatMap {
              case EmailAccepted => Future.successful(Right(EmailSentSuccess))
              case err           =>
                logger.warn(s"[EmailService][sendConfirmationEmail] Email not sent due to downstream error: ${err.toString}")
                Future.successful(Left(DownstreamError(err.toString)))
            }
          case _               =>
            logger.warn("[EmailService][sendConfirmationEmail] Email not sent due to missing data in minimal details")
            Future.successful(Left(MinimalDetailsError))
        }
      case _                                                    =>
        logger.warn("[EmailService][sendConfirmationEmail] Email not sent due to missing data in sd")
        Future.successful(Left(SessionDataError))
    }
  }

  private def format(instant: Instant): String = {
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
    val date          = localDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))
    val time          = localDateTime.format(DateTimeFormatter.ofPattern("HH:mma", Locale.ENGLISH))
    s"$date at ${time.toLowerCase}"
  }
}
