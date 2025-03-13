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

import javax.inject.Inject
import models.{AddressRecord, Mode, RecordSet}
import pages.MembersLastUkAddressLookupPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.MembersLastUkAddressLookupView

import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressLookupController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MembersLastUkAddressLookupFormProvider,
    val controllerComponents: MessagesControllerComponents,
    val addressLookupConnector: AddressLookupConnector,
    view: MembersLastUkAddressLookupView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  implicit object AddressOrdering extends Ordering[AddressRecord] {
    def compare(a: AddressRecord, b: AddressRecord): Int = a.id compare b.id
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      Ok(view(form, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, mode))),
      postcode =>
        addressLookupConnector.lookup(postcode).flatMap {
          case AddressLookupErrorResponse(e: BadRequestException) =>
            logger.warn(s"Bad request: ${e.message}")
            Future.successful(BadRequest(e.message))
          case AddressLookupErrorResponse(e)                      =>
            logger.warn(s"Internal error: $e")
            Future.successful(InternalServerError)
          case AddressLookupSuccessResponse(recordSet)            =>
            logger.info(s"Records: ${recordSet.toString}")
            Future.fromTry(request.userAnswers.set(MembersLastUkAddressLookupPage, RecordSet(recordSet.addresses.sorted))).flatMap { updatedAnswers =>
              sessionRepository.set(updatedAnswers).map { _ =>
                Redirect(MembersLastUkAddressLookupPage.nextPage(mode, updatedAnswers))
              }
            }
        }
    )
  }
}
