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
import services.CollectSubmittedVersionsService
import services.LockService
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import controllers.actions._
import views.html.viewandamend.SubmittedTransferSummaryView
import models._
import pages.memberDetails.MemberNamePage
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.SubmittedTransferSummaryViewModel

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class SubmittedTransferSummaryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  schemeData: SchemeDataAction,
  collectSubmittedVersionsService: CollectSubmittedVersionsService,
  val controllerComponents: MessagesControllerComponents,
  view: SubmittedTransferSummaryView,
  lockService: LockService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(
    qtReference: TransferId,
    pstr: PstrNumber,
    qtStatus: QtStatus,
    versionNumber: String
  ): Action[AnyContent] =
    (identify andThen schemeData).async { implicit request =>
      val owner = request.authenticatedUser match {
        case PsaUser(psaId, _, _) => psaId.value
        case PspUser(pspId, _, _) => pspId.value
      }

      for {
        isLocked <- lockService.isLocked(qtReference.value, owner)
        _        <- if (isLocked) lockService.releaseLock(qtReference.value, owner) else Future.unit

        result <- qtReference match {
                    case QtNumber(value) =>
                      collectSubmittedVersionsService.collectVersions(
                        qtReference,
                        pstr,
                        qtStatus,
                        versionNumber,
                        request.schemeDetails.srnNumber
                      ) map { case (maybeDraft, userAnswers) =>
                        def createTableRows    =
                          SubmittedTransferSummaryViewModel.rows(maybeDraft, userAnswers, versionNumber)
                        def memberName: String = if (userAnswers.nonEmpty) {
                          userAnswers.head.get(MemberNamePage) match {
                            case Some(name) => name.fullName
                            case None       => ""
                          }
                        } else {
                          ""
                        }

                        Ok(view(memberName, value, createTableRows))
                      }
                    case _               => Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                  }
      } yield result
    }
}
