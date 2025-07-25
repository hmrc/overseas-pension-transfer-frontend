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
import models.address.{AddressLookupResult, AddressRecords, NoAddressFound}
import org.apache.pekko.Done
import pages.memberDetails.MembersLastUkAddressLookupPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressService, UserAnswersService}
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
    view: MembersLastUkAddressLookupView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData) { implicit request =>
      val preparedForm = request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(AddressRecords(postcode, _)) => form.fill(postcode)
        case Some(NoAddressFound(postcode))    => form.fill(postcode)
        case _                                 => form
      }
      Ok(view(preparedForm, mode))
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
        postcode =>
          addressService.membersLastUkAddressLookup(postcode).flatMap {
            case None =>
              logger.warn(s"Address lookup failed for postcode: $postcode")
              Future.successful(
                Redirect(
                  MembersLastUkAddressLookupPage.nextPageRecovery(
                    Some(MembersLastUkAddressLookupPage.recoveryModeReturnUrl)
                  )
                )
              )

            case Some(result: AddressLookupResult) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersLastUkAddressLookupPage, result))
                _              <- sessionRepository.set(updatedAnswers)
                savedForLater  <- userAnswersService.setUserAnswers(updatedAnswers)
              } yield {
                savedForLater match {
                  case Right(Done) =>
                    result match {
                      case _: AddressRecords => Redirect(MembersLastUkAddressLookupPage.nextPage(mode, updatedAnswers))
                      case _: NoAddressFound => Redirect(MembersLastUkAddressLookupPage.nextPageNoResults())
                    }
                  case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                }

              }
          }
      )
    }
}
