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
import models.{MinimalDetails, SessionData}
import models.email.{EMAIL_ACCEPTED, EmailSendingResult, EmailToSendRequest, SubmissionConfirmation}
import play.api.i18n.Messages
import queries.EmailConfirmationQuery
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailService @Inject() (
    emailConnector: EmailConnector,
    clock: Clock
  )(implicit executionContext: ExecutionContext,
    appConfig: FrontendAppConfig
  ) {

  def sendConfirmationEmail(
      sessionData: SessionData,
      minimalDetails: MinimalDetails
    )(implicit hc: HeaderCarrier,
      messages: Messages
    ): Future[SessionData] = {
    if (appConfig.submissionEmailEnabled) {
      val recipientName = minimalDetails.individualDetails.map { id => s"${id.firstName} ${id.lastName}" }.getOrElse("Undefined")
      val emailAddress  = minimalDetails.email
      val amendmentDate = format(LocalDate.now(clock))

      val emailParameters = {
        SubmissionConfirmation(
          recipientName,
          amendmentDate
        )
      }

      emailConnector.send(
        EmailToSendRequest(
          List(emailAddress),
          appConfig.submittedConfirmationTemplateId,
          emailParameters
        )
      ) flatMap {
        emailConfirmationResult =>
          val emailSent = EMAIL_ACCEPTED == emailConfirmationResult
          Future.fromTry(sessionData.set(EmailConfirmationQuery, emailSent))
      }
    } else {
      val emailSent = false
      Future.fromTry(sessionData.set(EmailConfirmationQuery, emailSent))
    }
  }

  private def format(date: LocalDate) = {
    val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
    date.format(formatter)
  }
}
