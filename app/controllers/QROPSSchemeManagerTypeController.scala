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
import forms.QROPSSchemeManagerTypeFormProvider
import models.{Mode, NormalMode, QROPSSchemeManagerType}
import pages.{OrgIndividualNamePage, QROPSSchemeManagerTypePage, SchemeManagerOrganisationNamePage, SchemeManagersNamePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.QROPSSchemeManagerTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QROPSSchemeManagerTypeController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: QROPSSchemeManagerTypeFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: QROPSSchemeManagerTypeView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(QROPSSchemeManagerTypePage) match {
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
          val previousValue = request.userAnswers.get(QROPSSchemeManagerTypePage)
          for {
            baseAnswers <- Future.fromTry(request.userAnswers.set(QROPSSchemeManagerTypePage, value))

            // If the QROPSSchemeManagerType changes, remove the answers of previous corresponding questions
            updatedAnswers <- (previousValue, value) match {
                                case (Some(QROPSSchemeManagerType.Individual), QROPSSchemeManagerType.Organisation) => Future.fromTry(baseAnswers
                                    .remove(SchemeManagersNamePage))
                                case (Some(QROPSSchemeManagerType.Organisation), QROPSSchemeManagerType.Individual) => Future.fromTry(baseAnswers
                                    .remove(SchemeManagerOrganisationNamePage)
                                    .flatMap(_.remove(OrgIndividualNamePage)))
                                case _                                                                              => Future.successful(baseAnswers)
                              }
            _              <- sessionRepository.set(updatedAnswers)

            // If the QROPSSchemeManagerType changes, always switch to NormalMode to go through corresponding set of questions
            redirectMode = if (!previousValue.contains(value)) NormalMode else mode

          } yield Redirect(QROPSSchemeManagerTypePage.nextPage(redirectMode, updatedAnswers))
        }
      )
  }
}
