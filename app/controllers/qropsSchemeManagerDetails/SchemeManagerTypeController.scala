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

package controllers.qropsSchemeManagerDetails

import controllers.actions._
import controllers.helpers.ErrorHandling
import forms.qropsSchemeManagerDetails.SchemeManagerTypeFormProvider
import models.Mode
import models.TaskCategory.SchemeManagerDetails
import org.apache.pekko.Done
import pages.qropsSchemeManagerDetails.SchemeManagerTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.qropsSchemeManagerDetails.SchemeManagerTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SchemeManagerTypeController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    markInProgress: MarkInProgressOnEntryAction,
    formProvider: SchemeManagerTypeFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: SchemeManagerTypeView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen markInProgress.forCategoryAndMode(SchemeManagerDetails, mode) andThen displayData) {
      implicit request =>
        val preparedForm = request.userAnswers.get(SchemeManagerTypePage) match {
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
        value => {
          for {
            baseAnswers   <- Future.fromTry(request.userAnswers.set(SchemeManagerTypePage, value))
            _             <- sessionRepository.set(baseAnswers)
            savedForLater <- userAnswersService.setExternalUserAnswers(baseAnswers)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(SchemeManagerTypePage.nextPage(mode, baseAnswers))
              case Left(err)   => onFailureRedirect(err)
            }
          }
        }
      )
  }
}
