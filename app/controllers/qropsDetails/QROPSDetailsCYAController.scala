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

package controllers.qropsDetails

import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction, IsAssociatedCheckAction}
import controllers.helpers.ErrorHandling
import models.TaskCategory.QROPSDetails
import models.taskList.TaskStatus.Completed
import models.{CheckMode, NormalMode}
import org.apache.pekko.Done
import pages.qropsDetails.QROPSDetailsSummaryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.qropsDetails.QROPSDetailsCYAView

import scala.concurrent.{ExecutionContext, Future}

class QROPSDetailsCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    isAssociatedCheck: IsAssociatedCheckAction,
    sessionRepository: SessionRepository,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: QROPSDetailsCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen isAssociatedCheck) {
    implicit request =>
      val list = SummaryListViewModel(QROPSDetailsSummary.rows(CheckMode, request.userAnswers))

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen isAssociatedCheck).async {
    implicit request =>
      for {
        ua            <- Future.fromTry(request.userAnswers.set(TaskStatusQuery(QROPSDetails), Completed))
        _             <- sessionRepository.set(ua)
        savedForLater <- userAnswersService.setExternalUserAnswers(ua)
      } yield {
        savedForLater match {
          case Right(Done) => Redirect(QROPSDetailsSummaryPage.nextPage(NormalMode, ua))
          case Left(err)   => onFailureRedirect(err)
        }
      }
  }
}
