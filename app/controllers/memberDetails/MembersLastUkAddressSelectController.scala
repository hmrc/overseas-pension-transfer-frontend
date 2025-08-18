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
import forms.memberDetails.MembersLastUkAddressSelectFormProvider
import models.address.{AddressLookupResult, AddressRecords, MembersLookupLastUkAddress, NoAddressFound}
import models.{Mode, NormalMode}
import org.apache.pekko.Done
import pages.memberDetails.{MemberIsResidentUKPage, MembersLastUkAddressLookupPage, MembersLastUkAddressSelectPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.AddressViewModel
import views.html.memberDetails.MembersLastUkAddressSelectView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersLastUkAddressSelectController @Inject() (
    override val messagesApi: MessagesApi,
    appConfig: FrontendAppConfig,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MembersLastUkAddressSelectFormProvider,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressSelectView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData) { implicit request =>
      val fromFinalCYA: Boolean = request.request.headers.get(REFERER).getOrElse("/") == appConfig.finalCheckAnswersUrl

      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(AddressRecords(postcode, records)) =>
          val idAddressPairs  = records.map(r => (r.id, MembersLookupLastUkAddress.fromAddressRecord(r)))
          val ids             = idAddressPairs.map(_._1)
          val form            = formProvider(ids)
          val addressRadioSet = AddressViewModel.addressRadios(idAddressPairs)

          Ok(view(form, mode, addressRadioSet, postcode, fromFinalCYA))

        case Some(_: NoAddressFound) =>
          Redirect(MembersLastUkAddressSelectPage.nextPageRecovery(Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)))

        case None =>
          Redirect(routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url)
      }
    }

  def onSubmit(mode: Mode, fromFinalCYA: Boolean): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(AddressRecords(postcode, records)) =>
          val idAddressPairs  = records.map(r => (r.id, MembersLookupLastUkAddress.fromAddressRecord(r)))
          val ids             = idAddressPairs.map(_._1)
          val form            = formProvider(ids)
          val addressRadioSet = AddressViewModel.addressRadios(idAddressPairs)

          form.bindFromRequest().fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, addressRadioSet, postcode, fromFinalCYA))),
            selectedId =>
              records.find(_.id == selectedId) match {
                case Some(record) =>
                  val addressToSave = MembersLookupLastUkAddress.fromAddressRecord(record)
                  for {
                    updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersLastUkAddressSelectPage, addressToSave))
                    _              <- sessionRepository.set(updatedAnswers)
                    savedForLater  <- userAnswersService.setExternalUserAnswers(updatedAnswers)
                  } yield {
                    savedForLater match {
                      case Right(Done) => Redirect(MembersLastUkAddressSelectPage.nextPage(mode, updatedAnswers, fromFinalCYA))
                      case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                    }
                  }

                case None =>
                  Future.successful(
                    Redirect(MembersLastUkAddressSelectPage.nextPageRecovery(Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)))
                  )
              }
          )

        case Some(_: NoAddressFound) =>
          Future.successful(
            Redirect(MembersLastUkAddressSelectPage.nextPageRecovery(Some(MembersLastUkAddressSelectPage.recoveryModeReturnUrl)))
          )

        case None =>
          Future.successful(
            Redirect(routes.MembersLastUkAddressLookupController.onPageLoad(NormalMode).url)
          )
      }
    }
}
