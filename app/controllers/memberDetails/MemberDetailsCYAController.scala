/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import controllers.helpers.ErrorHandling
import models.TaskCategory.MemberDetails
import models.taskList.TaskStatus.Completed
import models.{CheckMode, NormalMode}
import org.apache.pekko.Done
import pages.memberDetails.MemberDetailsSummaryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.{TaskService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.memberDetails.MemberDetailsCYAView

import scala.concurrent.{ExecutionContext, Future}

class MemberDetailsCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService,
    taskService: TaskService,
    schemeData: SchemeDataAction,
    val controllerComponents: MessagesControllerComponents,
    view: MemberDetailsCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val list = SummaryListViewModel(MemberDetailsSummary.rows(CheckMode, request.userAnswers))

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      for {
        ua1           <- Future.fromTry(request.userAnswers.set(TaskStatusQuery(MemberDetails), Completed))
        _             <- sessionRepository.set(ua1)
        savedForLater <- userAnswersService.setExternalUserAnswers(ua1)
      } yield {
        savedForLater match {
          case Right(Done) => Redirect(MemberDetailsSummaryPage.nextPage(NormalMode, ua1))
          case Left(err)   => onFailureRedirect(err)
        }
      }
  }
}
