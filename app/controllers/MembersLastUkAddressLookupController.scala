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

import connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse}
import controllers.actions._
import forms.MembersLastUkAddressLookupFormProvider
import models.Mode
import models.address.{FoundAddressResponse, FoundAddressSet, NoAddressFound}
import pages.MembersLastUkAddressLookupPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MembersLastUkAddressLookupView

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
    val controllerComponents: MessagesControllerComponents,
    val addressLookupConnector: AddressLookupConnector,
    view: MembersLastUkAddressLookupView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case None        => form
        case Some(value) =>
          value match {
            case FoundAddressSet(searchedPostcode, _) => form.fill(searchedPostcode)
            case NoAddressFound(searchedPostcode)     => form.fill(searchedPostcode)
          }
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, mode))),
      postcode =>
        addressLookupConnector.lookup(postcode).flatMap {
          case AddressLookupErrorResponse(e)                             =>
            logger.warn(s"Error: $e")
            Future.successful(Redirect(
              MembersLastUkAddressLookupPage.nextPageRecovery(
                Some(MembersLastUkAddressLookupPage.recoveryModeReturnUrl)
              )
            ))
          case AddressLookupSuccessResponse(searchedPostcode, recordSet) =>
            val foundResponse: FoundAddressResponse = FoundAddressResponse.fromRecordSet(searchedPostcode, recordSet)
            foundResponse match {
              case fas: FoundAddressSet =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(MembersLastUkAddressLookupPage, fas)
                                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(MembersLastUkAddressLookupPage.nextPage(mode, updatedAnswers))
              case naf: NoAddressFound  =>
                for {
                  updatedAnswers <- Future.fromTry(
                                      request.userAnswers.set(MembersLastUkAddressLookupPage, naf)
                                    )
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(MembersLastUkAddressLookupPage.nextPageNoResults())
            }
        }
    )
  }
}
