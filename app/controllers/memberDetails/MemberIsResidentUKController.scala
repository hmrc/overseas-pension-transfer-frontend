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

package controllers.memberDetails

import services.TaskService
import services.UserAnswersService
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import forms.memberDetails.MemberIsResidentUKFormProvider
import controllers.actions._
import controllers.helpers.ErrorHandling
import org.apache.pekko.Done
import pages.memberDetails.MemberIsResidentUKPage
import play.api.data.Form
import models.TaskCategory.MemberDetails
import models.Mode
import views.html.memberDetails.MemberIsResidentUKView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class MemberIsResidentUKController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: MemberIsResidentUKFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MemberIsResidentUKView,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ErrorHandling {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.get(MemberIsResidentUKPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value =>
            for {
              baseAnswers   <- Future.fromTry(request.userAnswers.set(MemberIsResidentUKPage, value))
              ua1           <-
                Future.fromTry(TaskService.setInProgressInCheckMode(mode, baseAnswers, taskCategory = MemberDetails))
              savedForLater <-
                userAnswersService.setExternalUserAnswers(ua1, request.sessionData.schemeInformation.srnNumber)
            } yield savedForLater match {
              case Right(Done) => Redirect(MemberIsResidentUKPage.nextPage(mode, ua1))
              case Left(err)   => onFailureRedirect(err)
            }
        )
  }
}
