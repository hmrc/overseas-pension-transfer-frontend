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

import services.AddressService
import services.CountryService
import services.UserAnswersService
import utils.AppUtils
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import controllers.actions._
import play.api.Logging
import controllers.helpers.ErrorHandling
import models.Mode
import pages.qropsDetails.QROPSAddressPage
import org.apache.pekko.Done
import views.html.qropsDetails.QROPSAddressView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import forms.qropsDetails.QROPSAddressFormData
import forms.qropsDetails.QROPSAddressFormProvider
import models.requests.DisplayRequest
import play.api.data.Form

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

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
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with AppUtils
    with ErrorHandling {

  private def form(): Form[QROPSAddressFormData] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.get(QROPSAddressPage) match {
      case None          => form()
      case Some(address) => form().fill(QROPSAddressFormData.fromDomain(address))
    }

    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

    Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  private def renderErrorPage(formWithErrors: Form[QROPSAddressFormData], mode: Mode)(implicit
    displayRequest: DisplayRequest[_]
  ) = {
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
            case None          =>
              Future.successful(
                Redirect(QROPSAddressPage.nextPageRecovery(Some(QROPSAddressPage.recoveryModeReturnUrl)))
              )
            case Some(address) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(QROPSAddressPage, address))
                savedForLater  <- userAnswersService.setExternalUserAnswers(
                                    updatedAnswers,
                                    request.sessionData.schemeInformation.srnNumber
                                  )
              } yield savedForLater match {
                case Right(Done) => Redirect(QROPSAddressPage.nextPage(mode, updatedAnswers))
                case Left(err)   => onFailureRedirect(err)
              }
          }
      )
  }
}
