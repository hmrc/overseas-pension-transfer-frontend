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

import com.google.inject.Inject
import connectors.{PensionSchemeConnector, UserAnswersConnector}
import models.authentication.{AuthenticatedUser, PsaId}
import models.dtos.SubmissionDTO
import models.dtos.UserAnswersDTO.{fromUserAnswers, toUserAnswers}
import models.responses._
import models.{AllTransfersItem, PstrNumber, QtStatus, SessionData, TransferId, UserAnswers}
import org.apache.pekko.Done
import play.api.Logging
import play.api.libs.json._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ExecutionContext, Future}

class UserAnswersService @Inject() (
    connector: UserAnswersConnector,
    authService: AuthorisingPsaService
  )(implicit ec: ExecutionContext
  ) extends Logging {

  // These two versions of getExternalUserAnswers are purposely similar to one another as it is recommended to combine them in a future refactor
  def getExternalUserAnswers(sessionData: SessionData)(implicit hc: HeaderCarrier): Future[Either[UserAnswersError, UserAnswers]] = {
    connector.getAnswers(sessionData.transferId.value) map {
      case Right(userAnswersDTO) => Right(toUserAnswers(userAnswersDTO))
      case Left(error)           => Left(error)
    }
  }

  def getExternalUserAnswers(
      transferId: TransferId,
      pstr: PstrNumber,
      qtStatus: QtStatus,
      versionNumber: Option[String]
    )(implicit hc: HeaderCarrier
    ): Future[Either[UserAnswersError, UserAnswers]] = {
    connector.getAnswers(
      transferId,
      pstr,
      qtStatus,
      versionNumber
    ).map {
      case Right(dto) => Right(toUserAnswers(dto))
      case Left(err)  => Left(err)
    }
  }

  def setExternalUserAnswers(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Either[UserAnswersError, Done]] = {
    connector.putAnswers(fromUserAnswers(userAnswers))
  }

  def submitDeclaration(
      authenticatedUser: AuthenticatedUser,
      userAnswers: UserAnswers,
      sessionData: SessionData,
      maybePsaId: Option[PsaId] = None
    )(implicit hc: HeaderCarrier
    ): Future[Either[UserAnswersError, SubmissionResponse]] = {

    val submissionDTO = SubmissionDTO.fromRequest(authenticatedUser, userAnswers, maybePsaId, sessionData)
    maybePsaId match {
      case Some(psaId) =>
        authService.checkIsAuthorisingPsa(sessionData.schemeInformation.srnNumber.value, psaId).flatMap {
          case true  => connector.postSubmission(submissionDTO)
          case false =>
            Future.successful(Left(NotAuthorisingPsaIdErrorResponse("PSA is not PSP authorising PSA", None)))
        }
      case None        =>
        connector.postSubmission(submissionDTO)
    }
  }

  def clearUserAnswers(id: String)(implicit hc: HeaderCarrier): Future[Either[UserAnswersError, Done]] = {
    connector.deleteAnswers(id)
  }

  def toAllTransfersItem(userAnswers: UserAnswers): AllTransfersItem = {
    val reportDetails = (userAnswers.data \ "reportDetails").asOpt[JsObject]
    val qtStatusOpt   = reportDetails
      .flatMap(obj => (obj \ "qtStatus").asOpt[String])
      .flatMap(QtStatus.parse)

    val ninoOpt         = (userAnswers.data \ "memberDetails" \ "nino").asOpt[String]
    val memberFirstName = (userAnswers.data \ "memberDetails" \ "name" \ "firstName").asOpt[String]
    val memberSurname   = (userAnswers.data \ "memberDetails" \ "name" \ "lastName").asOpt[String]

    val qtStatus = qtStatusOpt.orElse(Some(QtStatus.InProgress))

    val submissionDate =
      if (qtStatus.contains(QtStatus.Submitted)) Some(userAnswers.lastUpdated) else None
    val lastUpdated    =
      if (qtStatus.exists(_ != QtStatus.Submitted)) Some(userAnswers.lastUpdated) else None

    AllTransfersItem(
      transferId      = userAnswers.id,
      qtVersion       = None,
      qtStatus        = qtStatus,
      nino            = ninoOpt,
      memberFirstName = memberFirstName,
      memberSurname   = memberSurname,
      qtDate          = None,
      lastUpdated     = lastUpdated,
      pstrNumber      = Some(userAnswers.pstr),
      submissionDate  = submissionDate
    )
  }
}
