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

import controllers.actions._
import forms.memberDetails.MembersLastUkAddressLookupFormProvider
import models.Mode
import models.address.{FoundAddressSet, NoAddressFound}
import pages.memberDetails.MembersLastUkAddressLookupPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AddressService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.memberDetails.MembersLastUkAddressLookupView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressLookupController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MembersLastUkAddressLookupFormProvider,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressLookupView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData) { implicit request =>
      val preparedForm = request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case None        => form
        case Some(value) =>
          value match {
            case FoundAddressSet(pc, _) => form.fill(pc)
            case NoAddressFound(pc)     => form.fill(pc)
          }
      }
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        postcode =>
          addressService.membersLastUkAddressLookup(postcode).flatMap {
            case None         =>
              Future.successful(
                Redirect(MembersLastUkAddressLookupPage.nextPageRecovery(
                  Some(MembersLastUkAddressLookupPage.recoveryModeReturnUrl)
                ))
              )
            case Some(result) =>
              result match {
                case fas: FoundAddressSet =>
                  for {
                    userAnswers <- Future.fromTry(request.userAnswers.set(MembersLastUkAddressLookupPage, fas))
                    _           <- sessionRepository.set(userAnswers)
                  } yield Redirect(MembersLastUkAddressLookupPage.nextPage(mode, userAnswers))

                case naf: NoAddressFound =>
                  for {
                    userAnswers <- Future.fromTry(request.userAnswers.set(MembersLastUkAddressLookupPage, naf))
                    _           <- sessionRepository.set(userAnswers)
                  } yield Redirect(MembersLastUkAddressLookupPage.nextPageNoResults())
              }
          }
      )
    }
}
