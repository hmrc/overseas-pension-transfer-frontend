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

import config.FrontendAppConfig
import controllers.actions._
import forms.memberDetails.MemberHasEverBeenResidentUKFormProvider
import models.Mode
import org.apache.pekko.Done
import pages.memberDetails.MemberHasEverBeenResidentUKPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{MemberDetailsService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.memberDetails.MemberHasEverBeenResidentUKView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MemberHasEverBeenResidentUKController @Inject() (
    override val messagesApi: MessagesApi,
    appConfig: FrontendAppConfig,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    memberDetailsService: MemberDetailsService,
    formProvider: MemberHasEverBeenResidentUKFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberHasEverBeenResidentUKView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form: Form[Boolean] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val fromFinalCYA: Boolean = request.request.headers.get(REFERER).getOrElse("/") == appConfig.finalCheckAnswersUrl

      val preparedForm = request.userAnswers.get(MemberHasEverBeenResidentUKPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, fromFinalCYA))
  }

  def onSubmit(mode: Mode, fromFinalCYA: Boolean): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, fromFinalCYA))),
        value => {
          val previousValue = request.userAnswers.get(MemberHasEverBeenResidentUKPage)
          for {
            baseAnswers    <- Future.fromTry(request.userAnswers.set(MemberHasEverBeenResidentUKPage, value))
            updatedAnswers <- memberDetailsService.updateMemberHasEverBeenResidentUKAnswers(baseAnswers, previousValue, value)
            _              <- sessionRepository.set(updatedAnswers)
            savedForLater  <- userAnswersService.setExternalUserAnswers(updatedAnswers)
            redirectMode    = memberDetailsService.getMemberHasEverBeenResidentUKRedirectMode(mode, previousValue, value)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(MemberHasEverBeenResidentUKPage.nextPage(redirectMode, updatedAnswers, fromFinalCYA))
              case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
        }
      )
  }
}
