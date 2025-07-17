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
import forms.qropsSchemeManagerDetails.SchemeManagerTypeFormProvider
import models.Mode
import org.apache.pekko.Done
import pages.memberDetails.MemberIsResidentUKPage
import pages.qropsSchemeManagerDetails.SchemeManagerTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{SchemeManagerService, UserAnswersService}
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
    schemeManagerService: SchemeManagerService,
    formProvider: SchemeManagerTypeFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: SchemeManagerTypeView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
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
          val previousValue = request.userAnswers.get(SchemeManagerTypePage)
          for {
            baseAnswers    <- Future.fromTry(request.userAnswers.set(SchemeManagerTypePage, value))
            updatedAnswers <- schemeManagerService.updateSchemeManagerTypeAnswers(baseAnswers, previousValue, value)
            redirectMode    = schemeManagerService.getSchemeManagerTypeRedirectMode(mode, previousValue, value)
            _              <- sessionRepository.set(updatedAnswers)
            savedForLater  <- userAnswersService.setUserAnswers(updatedAnswers)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(SchemeManagerTypePage.nextPage(redirectMode, updatedAnswers))
              case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
        }
      )
  }
}
