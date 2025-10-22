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

package controllers

import controllers.actions._
import models.{PstrNumber, QtStatus}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ViewAmendSelectorView
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.mongo.lock.LockRepository
import config.FrontendAppConfig
import scala.concurrent.duration.DurationLong
import pages.memberDetails.MemberNamePage
import services.UserAnswersService

class ViewAmendSelectorController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    val controllerComponents: MessagesControllerComponents,
    view: ViewAmendSelectorView,
    appConfig: FrontendAppConfig,
    lockRepository: LockRepository,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val lockTtlSeconds: Long = appConfig.dashboardLockTtl

  def onPageLoad(qtReference: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData andThen getData) { implicit request =>
      Ok(view(qtReference, pstr, qtStatus, versionNumber)).withSession(
        request.session +
          ("qtReference"   -> qtReference) +
          ("pstr"          -> pstr.value) +
          ("qtStatus"      -> qtStatus.toString) +
          ("versionNumber" -> versionNumber)
      )
    }

  def onSubmit(qtReference: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      for {
        formData <- Future.successful(
                      request.body.asFormUrlEncoded
                        .flatMap(_.get("option").flatMap(_.headOption))
                    )
        result   <- formData match {
                      case Some("view")  =>
                        Future.successful(Redirect(routes.ViewSubmittedController.fromDashboard(qtReference, pstr, qtStatus, versionNumber)))

                      // TODO - implement routing for amend functionality
                      case Some("amend") =>
                        val internalId = request.authenticatedUser.internalId
                        for {
                          userAnswersResult <- userAnswersService.getExternalUserAnswers(None, Some(qtReference), pstr, qtStatus, Some(versionNumber))
                          _                 <- Future.successful(lockRepository.releaseLock(qtReference, internalId))
                          lockResult        <- lockRepository.takeLock(qtReference, internalId, lockTtlSeconds.seconds)
                        } yield lockResult match {
                          case Some(_) => Redirect(controllers.routes.TaskListController.onPageLoad())
                          case None    => Redirect(routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber))
                              .flashing("lockWarning" -> userAnswersResult.toOption.flatMap(_.get(MemberNamePage)).map(_.fullName).getOrElse(qtReference))
                        }

                      case Some("") | None =>
                        Future.successful(Redirect(routes.ViewAmendSelectorController.onPageLoad(qtReference, pstr, qtStatus, versionNumber))
                          .flashing("error" -> "true"))

                      case _ =>
                        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
                    }
      } yield result
    }
}
