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

package controllers.actions

import connectors.PensionSchemeConnector
import controllers.routes
import models.authentication.{AuthenticatedUser, Psa, PsaUser, PspUser}
import models.requests.{DataRequest, DisplayRequest}
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionBuilder, ActionRefiner, ActionTransformer, Result}
import queries.mps.SrnQuery
import repositories.DashboardSessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AppUtils

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsAssociatedCheckActionImpl @Inject()(
    pensionSchemeConnector: PensionSchemeConnector,
    dashboardSessionRepository: DashboardSessionRepository
                                      )(implicit val executionContext: ExecutionContext) extends IsAssociatedCheckAction with AppUtils {

  override protected def refine[A](request: DataRequest[A]): Future[Either[Result, DisplayRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    dashboardSessionRepository.get(request.authenticatedUser.internalId) flatMap {
      case Some(dashboardData) =>
        dashboardData.get(SrnQuery) match {
          case Some(srn) =>
            pensionSchemeConnector.checkAssociation(srn.value, request.authenticatedUser) map {
              isAssociated =>
                if (isAssociated) {
                  def authedUser: AuthenticatedUser = {
                    request.authenticatedUser match {
                      case PsaUser(userId, id, None) => PsaUser(userId, id, Some(srn))
                      case PspUser(userId, id, None) => PspUser(userId, id, Some(srn))
                      case user@PsaUser(_, _, _) => user
                      case user@PspUser(_, _, _) => user
                    }
                  }

                  Right(
                    DisplayRequest(
                      request.request,
                      authedUser,
                      request.userAnswers,
                      memberFullName(request.userAnswers),
                      qtNumber(request.userAnswers),
                      dateTransferSubmitted(request.userAnswers)
                    )
                  )
                } else {
                  Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
                }
            }
        }
      case None => Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

    }
  }
}

trait IsAssociatedCheckAction extends ActionRefiner[DataRequest, DisplayRequest]
