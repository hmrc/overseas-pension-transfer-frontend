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

package controllers.qropsDetails

import config.FrontendAppConfig
import controllers.actions._
import controllers.helpers.ErrorHandling
import forms.qropsDetails.{QROPSAddressFormData, QROPSAddressFormProvider}
import models.Mode
import models.requests.DisplayRequest
import org.apache.pekko.Done
import pages.qropsDetails.QROPSAddressPage
import play.api.Logging
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressService, CountryService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.CountrySelectViewModel
import views.html.qropsDetails.QROPSAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QROPSAddressController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    getData: DataRetrievalAction,
    formProvider: QROPSAddressFormProvider,
    countryService: CountryService,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: QROPSAddressView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext,
    appConfig: FrontendAppConfig
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils with ErrorHandling {

  private def form(): Form[QROPSAddressFormData] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(QROPSAddressPage) match {
        case None          => form()
        case Some(address) => form().fill(QROPSAddressFormData.fromDomain(address))
      }

      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

      Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def renderErrorPage(formWithErrors: Form[QROPSAddressFormData], mode: Mode)(implicit displayRequest: DisplayRequest[_]) = {
    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
    Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      val boundForm = form().bindFromRequest()

      boundForm.fold(
        formWithErrors => renderErrorPage(formWithErrors, mode),
        formData =>
          addressService.qropsAddress(formData) match {
            case None                                                                                                                    =>
              Future.successful(
                Redirect(QROPSAddressPage.nextPageRecovery(Some(QROPSAddressPage.recoveryModeReturnUrl)))
              )
            case Some(address) if address.addressLine4.nonEmpty && address.country.code != "GB" && appConfig.accessibilityAddressChanges =>
              renderErrorPage(
                boundForm.withError(FormError(
                  "addressLine4",
                  "membersLastUKAddress.error.postcode.incorrect"
                )),
                mode
              )
            case Some(address)                                                                                                           =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(QROPSAddressPage, address))
                savedForLater  <- userAnswersService.setExternalUserAnswers(updatedAnswers)
              } yield {
                savedForLater match {
                  case Right(Done) => Redirect(QROPSAddressPage.nextPage(mode, updatedAnswers))
                  case Left(err)   => onFailureRedirect(err)
                }
              }
          }
      )
  }
}
