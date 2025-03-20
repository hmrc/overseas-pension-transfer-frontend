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
import forms.MembersLastUKAddressFormProvider
import models.address._
import models.{Mode, UserAnswers}
import pages.MembersLastUKAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import views.html.MembersLastUKAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersLastUKAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MembersLastUKAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUKAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils {

  private def form(userAnswers: UserAnswers): Form[MembersLastUKAddress] = formProvider(userAnswers)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers  = request.userAnswers
      val preparedForm = userAnswers.get(MembersLastUKAddressPage) match {
        case None          => form(userAnswers)
        case Some(address) => form(userAnswers).fill(
            MembersLastUKAddress.fromAddress(address)
          )
      }

      Ok(view(preparedForm, memberFullName(userAnswers), mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form(request.userAnswers).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, memberFullName(request.userAnswers), mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersLastUKAddressPage, value))
            _              <- {
              logger.info(Json.stringify(updatedAnswers.data))
              sessionRepository.set(updatedAnswers)
            }
          } yield Redirect(MembersLastUKAddressPage.nextPage(mode, updatedAnswers))
      )
  }
}
