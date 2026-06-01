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

import models.authentication.AuthenticatedUser
import utils.AuthSupport
import play.api.mvc._
import com.google.inject.Inject
import config.FrontendAppConfig
import uk.gov.hmrc.auth.core.retrieve.~
import play.api.Logging
import uk.gov.hmrc.http.HeaderCarrier
import play.api.mvc.Results._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.affinityGroup
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.allEnrolments
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.internalId
import controllers.auth.routes
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import models.requests.IdentifierRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent]

class IdentifierActionImpl @Inject() (
  override val authConnector: AuthConnector,
  config: FrontendAppConfig,
  val parser: BodyParsers.Default
)(implicit val executionContext: ExecutionContext)
    extends IdentifierAction
    with AuthorisedFunctions
    with AuthSupport
    with Logging {

  private def predicate: Predicate = buildPredicate(config)

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(predicate).retrieve(internalId and allEnrolments and affinityGroup) {
      case optInternalId ~ enrolments ~ Some(affinityGroup) =>
        if (affinityGroup == AffinityGroup.Agent) {
          throw AgentAffinityGroupNotAllowed
        }
        val internalId                           = getOrElseFailWithUnauthorised(optInternalId, "Unable to retrieve internalId")
        val authenticatedUser: AuthenticatedUser = extractUser(enrolments, config, internalId, affinityGroup)
        block(IdentifierRequest(request, authenticatedUser))
      case _ ~ _ ~ None                                     => throw MissingAffinityGroup
    } recover handleAuthException
  }

  private def handleAuthException: PartialFunction[Throwable, Result] = {
    case _: NoActiveSession =>
      Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl)))

    case AgentAffinityGroupNotAllowed =>
      logger.warn("Agent users are not permitted to access this service")
      Redirect(routes.UnauthorisedController.onPageLoad())
    case MissingAffinityGroup         =>
      logger.warn("Affinity group required to access this service")
      Redirect(routes.UnauthorisedController.onPageLoad())
    case e                            =>
      logger.error("Unexpected error during authorisation", e)
      Redirect(routes.UnauthorisedController.onPageLoad())
  }

  private case object AgentAffinityGroupNotAllowed extends RuntimeException("Agent affinity group is not supported")
  private case object MissingAffinityGroup extends RuntimeException("Affinity group is missing")

}
