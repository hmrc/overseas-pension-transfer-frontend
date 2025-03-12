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
import forms.MemberSelectLastUkAddressFormProvider

import javax.inject.Inject
import models.{Mode, NormalMode, UserAnswers}
import pages.{MemberSelectLastUkAddressPage, MembersLastUkAddressLookupPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{JourneyRecoveryContinueView, MemberSelectLastUkAddressView}

import scala.concurrent.{ExecutionContext, Future}

class MemberSelectLastUkAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    recoveryView: JourneyRecoveryContinueView,
    formProvider: MemberSelectLastUkAddressFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MemberSelectLastUkAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(addressRecords) => {
          val ukAddresses = addressRecords.addresses.map { ar => ar.address }
          Ok(view(form, mode, ukAddresses))
        }
        case None                 => BadRequest(recoveryView(routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(recoveryView(routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MemberSelectLastUkAddressPage, value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(MemberSelectLastUkAddressPage.nextPage(mode, updatedAnswers))
      )
  }
}
