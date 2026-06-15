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

package controllers.viewandamend

import models.authentication.PsaUser
import models.authentication.PspUser
import services.LockService
import services.UserAnswersService
import models.QtStatus.AmendInProgress
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import config.FrontendAppConfig
import controllers.actions._
import views.html.viewandamend.ViewAmendSelectorView
import play.api.libs.json.Json
import repositories.SessionRepository
import pages.memberDetails.MemberNamePage
import models._
import models.audit.JourneyStartedType.StartAmendmentOfTransfer
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import forms.viewandamend.ViewAmendSelectorFormProvider
import models.requests.SchemeRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import java.time.Clock
import java.time.Instant
import javax.inject.Inject

class ViewAmendSelectorController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  schemeData: SchemeDataAction,
  val controllerComponents: MessagesControllerComponents,
  view: ViewAmendSelectorView,
  appConfig: FrontendAppConfig,
  lockService: LockService,
  userAnswersService: UserAnswersService,
  sessionRepository: SessionRepository,
  clock: Clock
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl
  private val form                 = ViewAmendSelectorFormProvider.form()

  private def retrieveOwner(implicit request: SchemeRequest[AnyContent]) = request.authenticatedUser match {
    case PsaUser(psaId, _, _) => psaId.value
    case PspUser(pspId, _, _) => pspId.value
  }

  def onPageLoad(
    qtReference: TransferId,
    pstr: PstrNumber,
    qtStatus: QtStatus,
    versionNumber: String
  ): Action[AnyContent] =
    (identify andThen schemeData).async { implicit request =>
      val owner = retrieveOwner
      for {
        isLocked <- lockService.isLocked(qtReference.value, owner)
        _        <- if (isLocked) lockService.releaseLock(qtReference.value, owner) else Future.unit
      } yield Ok(view(qtReference, pstr, qtStatus, versionNumber, form)).withSession(
        request.session +
          ("qtReference"   -> qtReference.value) +
          ("pstr"          -> pstr.value) +
          ("qtStatus"      -> qtStatus.toString) +
          ("versionNumber" -> versionNumber)
      )
    }

  private def lockAndStartAmend(qtReference: TransferId, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String)(
    implicit request: SchemeRequest[AnyContent]
  ) = {
    val owner = retrieveOwner
    for {
      userAnswersResult <-
        userAnswersService.getExternalUserAnswers(
          qtReference,
          pstr,
          AmendInProgress,
          Some(versionNumber),
          request.schemeDetails.srnNumber
        )
      allTransfersItem   = userAnswersResult.toOption.map(userAnswersService.toAllTransfersItem)
      lockResult        <-
        lockService.takeLockWithAudit(
          qtReference,
          owner,
          lockTtlSeconds,
          request.authenticatedUser,
          request.schemeDetails,
          StartAmendmentOfTransfer,
          allTransfersItem
        )
    } yield (userAnswersResult, lockResult) match {
      case (Right(answers), true) =>
        val sessionData = SessionData(
          request.authenticatedUser.internalId,
          qtReference,
          request.schemeDetails,
          request.authenticatedUser,
          Json.obj(
            "receiptDate" -> answers.lastUpdated
          ),
          Instant.now(clock)
        )
        sessionRepository.set(sessionData)
        Redirect(routes.ViewAmendSubmittedController.amend()).withSession(request.session + ("isAmend" -> "true"))
      case (_, false)             =>
        val memberName =
          userAnswersResult.toOption.flatMap(_.get(MemberNamePage)).map(_.fullName).getOrElse(qtReference.value)
        Redirect(routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber))
          .flashing("lockWarning" -> memberName)
      case _                      =>
        Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
    }
  }

  def onSubmit(
    qtReference: TransferId,
    pstr: PstrNumber,
    qtStatus: QtStatus,
    versionNumber: String
  ): Action[AnyContent] =
    (identify andThen schemeData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(qtReference, pstr, qtStatus, versionNumber, formWithErrors))),
          {
            case Some("view")  =>
              Future.successful(
                Redirect(routes.ViewAmendSubmittedController.view(qtReference, pstr, qtStatus, versionNumber))
              )
            case Some("amend") =>
              lockAndStartAmend(qtReference, pstr, qtStatus, versionNumber)
            case _             =>
              Future.successful(
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              )

          }
        )
    }

}
