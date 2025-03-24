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
import forms.MembersCurrentAddressFormProvider

import javax.inject.Inject
import models.Mode
import models.address.MembersCurrentAddress
import pages.MembersCurrentAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import views.html.MembersCurrentAddressView

import scala.concurrent.{ExecutionContext, Future}

class MembersCurrentAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MembersCurrentAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MembersCurrentAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils {

  private def form(memberName: String)(implicit messages: Messages): Form[MembersCurrentAddress] = formProvider(memberName)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers  = request.userAnswers
      val memberName   = memberFullName(request.userAnswers)
      val preparedForm = userAnswers.get(MembersCurrentAddressPage) match {
        case None          => form(memberName)
        case Some(address) => form(memberName).fill(MembersCurrentAddress.fromAddress(address))
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val memberName = memberFullName(request.userAnswers)
      form(memberName).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, memberName, mode))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersCurrentAddressPage, value))
            _              <- {
              logger.info(Json.stringify(updatedAnswers.data))
              sessionRepository.set(updatedAnswers)
            }
          } yield Redirect(MembersCurrentAddressPage.nextPage(mode, updatedAnswers))
      )
  }
}
