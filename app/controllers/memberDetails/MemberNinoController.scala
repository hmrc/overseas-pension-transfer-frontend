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

import connectors.{UserAnswersConnector, UserAnswersErrorResponse, UserAnswersSuccessResponse}
import controllers.actions._
import forms.memberDetails.MemberNinoFormProvider
import models.Mode
import models.dtos.UserAnswersDTO
import pages.memberDetails.{MemberDoesNotHaveNinoPage, MemberNinoPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.memberDetails.MemberNinoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MemberNinoController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MemberNinoFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberNinoView,
    userAnswersConnector: UserAnswersConnector
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MemberNinoPage) match {
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
            ninoUserAnswers  <- Future.fromTry(request.userAnswers.set(MemberNinoPage, value).flatMap(_.remove(MemberDoesNotHaveNinoPage)))
            // TODO: look into best way to implement headers
            hc: HeaderCarrier = new HeaderCarrier()
            backendResponse  <- userAnswersConnector.putAnswers(ninoUserAnswers.id, UserAnswersDTO.fromUserAnswers(ninoUserAnswers))(hc, ec)
            updatedAnswers    = backendResponse match {
                                  case UserAnswersSuccessResponse(userAnswersDTO) =>
                                    UserAnswersDTO.toUserAnswers(userAnswersDTO)
                                  case UserAnswersErrorResponse(error)            =>
                                    // TODO: how to fail gracefully here
                                    logger.warn(s"Failed to store user answers in backend: ${error.getMessage}", error)
                                    ninoUserAnswers
                                }
            _                <- {
              logger.info(Json.stringify(updatedAnswers.data))
              sessionRepository.set(updatedAnswers)
            }
          } yield Redirect(MemberNinoPage.nextPage(mode, updatedAnswers))
      )
  }
}
