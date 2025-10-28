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

import config.FrontendAppConfig
import controllers.actions._
import models.QtStatus.AmendInProgress
import models.authentication.{PsaUser, PspUser}
import models.{PstrNumber, QtStatus, SessionData, TransferId}
import pages.memberDetails.MemberNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.mongo.lock.LockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.viewandamend.ViewAmendSelectorView

import javax.inject.Inject
import scala.concurrent.duration.DurationLong
import scala.concurrent.{ExecutionContext, Future}

class ViewAmendSelectorController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    val controllerComponents: MessagesControllerComponents,
    view: ViewAmendSelectorView,
    appConfig: FrontendAppConfig,
    lockRepository: LockRepository,
    userAnswersService: UserAnswersService,
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  def onPageLoad(qtReference: TransferId, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData) { implicit request =>
      Ok(view(qtReference, pstr, qtStatus, versionNumber)).withSession(
        request.session +
          ("qtReference"   -> qtReference.value) +
          ("pstr"          -> pstr.value) +
          ("qtStatus"      -> qtStatus.toString) +
          ("versionNumber" -> versionNumber)
      )
    }

  def onSubmit(qtReference: TransferId, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData).async { implicit request =>
      for {
        formData <- Future.successful(
                      request.body.asFormUrlEncoded
                        .flatMap(_.get("option").flatMap(_.headOption))
                    )
        result   <- formData match {

                      case Some("view") =>
                        Future.successful(Redirect(routes.ViewAmendSubmittedController.view(qtReference, pstr, qtStatus, versionNumber)))

                      case Some("") | None =>
                        Future.successful(
                          Redirect(routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber))
                            .flashing("error" -> "true")
                        )

                      case Some("amend") =>
                        val owner = request.authenticatedUser match {
                          case PsaUser(psaId, _, _, _) => psaId.value
                          case PspUser(pspId, _, _, _) => pspId.value
                        }
                        for {
                          userAnswersResult <- userAnswersService.getExternalUserAnswers(qtReference, pstr, AmendInProgress, Some(versionNumber))
                          lockResult        <- lockRepository.takeLock(qtReference.value, owner, lockTtlSeconds.seconds)
                        } yield (userAnswersResult, lockResult) match {
                          case (Right(answers), Some(_)) =>
                            val sessionData = SessionData(
                              request.authenticatedUser.internalId,
                              qtReference,
                              request.authenticatedUser.pensionSchemeDetails.get,
                              request.authenticatedUser,
                              Json.obj()
                            )

                            val sessionDataWithMemberName: SessionData = answers.get(MemberNamePage).fold(sessionData) {
                              name =>
                                sessionData.set(MemberNamePage, name).getOrElse(sessionData)
                            }

                            sessionRepository.set(sessionDataWithMemberName)
                            Redirect(routes.ViewAmendSubmittedController.amend())
                              .withSession(request.session + ("isAmend" -> "true"))

                          case (_, None) =>
                            val memberName = userAnswersResult.toOption
                              .flatMap(_.get(MemberNamePage))
                              .map(_.fullName)
                              .getOrElse(qtReference.value)

                            Redirect(routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber))
                              .flashing("lockWarning" -> memberName)

                          case _ =>
                            Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                        }
                    }
      } yield result
    }
}
