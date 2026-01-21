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

import com.google.inject.Inject
import connectors.PensionSchemeConnector
import controllers.routes
import models.requests.{IdentifierRequest, SchemeRequest}
import models.{PensionSchemeDetails, SrnNumber}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import queries.PensionSchemeDetailsQuery
import repositories.DashboardSessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.AppUtils

import scala.concurrent.{ExecutionContext, Future}

class SchemeDataActionImpl @Inject() (
    pensionSchemeConnector: PensionSchemeConnector,
    dashboardSessionRepository: DashboardSessionRepository
  )(implicit val executionContext: ExecutionContext
  ) extends SchemeDataAction with AppUtils with Logging {

  override protected def refine[A](request: IdentifierRequest[A]): Future[Either[Result, SchemeRequest[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    request.getQueryString("srn") match {
      case Some(value) =>
        pensionSchemeConnector.checkAssociation(value, request.authenticatedUser) flatMap {
          isAssociated =>
            if (isAssociated) {
              pensionSchemeConnector.getSchemeDetails(value, request.authenticatedUser) map {
                case Right(pensionSchemeResponse) =>
                  Right(
                    SchemeRequest(
                      request           = request,
                      authenticatedUser = request.authenticatedUser,
                      schemeDetails     = PensionSchemeDetails(SrnNumber(value), pensionSchemeResponse.pstr, pensionSchemeResponse.schemeName)
                    )
                  )
                case Left(err)                    =>
                  logger.error(s"[SchemeDataAction][refine]: Error has occurred during request of scheme details: $err")
                  Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
              }
            } else {
              logger.error(s"[SchemeDataAction][refine]: User isn't associated to a scheme")
              Future.successful(Left(Redirect(controllers.auth.routes.UnauthorisedController.onPageLoad())))
            }
        }
      case None        =>
        dashboardSessionRepository.get(request.authenticatedUser.internalId) flatMap {
          case Some(dashboardData) =>
            dashboardData.get(PensionSchemeDetailsQuery) match {
              case Some(scheme @ PensionSchemeDetails(srn, _, _)) =>
                pensionSchemeConnector.checkAssociation(srn.value, request.authenticatedUser) map {
                  isAssociated =>
                    if (isAssociated) {
                      Right(
                        SchemeRequest(
                          request           = request,
                          authenticatedUser = request.authenticatedUser,
                          schemeDetails     = scheme
                        )
                      )
                    } else {
                      Left(Redirect(controllers.auth.routes.UnauthorisedController.onPageLoad()))
                    }
                }
              case None                                           =>
                logger.error(s"[SchemeDataAction][refine]: Dashboard Data requires PensionSchemeDetails")
                Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))

            }
          case None                => Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
        }
    }
  }
}

trait SchemeDataAction extends ActionRefiner[IdentifierRequest, SchemeRequest]
