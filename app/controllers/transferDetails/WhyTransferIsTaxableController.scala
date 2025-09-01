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

package controllers.transferDetails

import controllers.actions._
import forms.transferDetails.WhyTransferIsTaxableFormProvider
import models.{CheckMode, Mode}
import models.TaskCategory.{MemberDetails, TransferDetails}
import models.taskList.TaskStatus.InProgress
import org.apache.pekko.Done
import pages.memberDetails.MemberIsResidentUKPage
import pages.transferDetails.WhyTransferIsTaxablePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.{TaskService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.WhyTransferIsTaxableView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WhyTransferIsTaxableController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    taskService: TaskService,
    formProvider: WhyTransferIsTaxableFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: WhyTransferIsTaxableView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhyTransferIsTaxablePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            ua1           <- Future.fromTry(request.userAnswers.set(WhyTransferIsTaxablePage, value))
            ua2           <- Future.fromTry(taskService.setInProgressInCheckMode(mode, ua1, TransferDetails))
            _             <- sessionRepository.set(ua2)
            savedForLater <- userAnswersService.setExternalUserAnswers(ua2)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(WhyTransferIsTaxablePage.nextPage(mode, ua2))
              case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
      )
  }
}
