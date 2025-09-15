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
import models.PensionSchemeDetails
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import queries.PensionSchemeDetailsQuery
import repositories.DashboardSessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AppUtils

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeDataActionImpl @Inject() (
    pensionSchemeConnector: PensionSchemeConnector,
    dashboardSessionRepository: DashboardSessionRepository
  )(implicit val executionContext: ExecutionContext
  ) extends SchemeDataAction with AppUtils with Logging {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, IdentifierRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    if (request.authenticatedUser.pensionSchemeDetails.isEmpty) {
      dashboardSessionRepository.get(request.authenticatedUser.internalId) flatMap {
        case Some(dashboardData) =>
          dashboardData.get(PensionSchemeDetailsQuery) match {
            case Some(scheme @ PensionSchemeDetails(srn, _, _)) =>
              pensionSchemeConnector.checkAssociation(srn.value, request.authenticatedUser) map {
                isAssociated =>
                  if (isAssociated) {
                    Right(
                      IdentifierRequest(
                        request.request,
                        request.authenticatedUser.updatePensionSchemeDetails(scheme)
                      )
                    )
                  } else {
                    Left(Redirect(controllers.auth.routes.UnauthorisedController.onPageLoad()))
                  }
              }
            case None                                           => Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
          }
        case None                => Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

      }
    } else {
      Future.successful(Right(request))
    }
  }
}

trait SchemeDataAction extends ActionRefiner[IdentifierRequest, IdentifierRequest]
