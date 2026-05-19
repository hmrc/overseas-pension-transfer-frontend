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

package controllers.qropsSchemeManagerDetails

import services.AddressService
import services.CountryService
import services.UserAnswersService
import utils.AppUtils
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormData
import forms.qropsSchemeManagerDetails.SchemeManagersAddressFormProvider
import controllers.actions._
import pages.qropsSchemeManagerDetails.SchemeManagersAddressPage
import play.api.Logging
import controllers.helpers.ErrorHandling
import models.Mode
import org.apache.pekko.Done
import views.html.qropsSchemeManagerDetails.SchemeManagersAddressView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import models.requests.DisplayRequest
import play.api.data.Form

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class SchemeManagersAddressController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: SchemeManagersAddressFormProvider,
  countryService: CountryService,
  addressService: AddressService,
  val controllerComponents: MessagesControllerComponents,
  view: SchemeManagersAddressView,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging
    with AppUtils
    with ErrorHandling {

  private def form: Form[SchemeManagersAddressFormData] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val userAnswers  = request.userAnswers
    val preparedForm = userAnswers.get(SchemeManagersAddressPage) match {
      case None          => form
      case Some(address) => form.fill(SchemeManagersAddressFormData.fromDomain(address))
    }

    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

    Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  private def returnErrorPage(formWithErrors: Form[SchemeManagersAddressFormData], mode: Mode)(implicit
    request: DisplayRequest[_]
  ) = {
    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
    Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      val boundForm = form.bindFromRequest()

      boundForm.fold(
        formWithErrors => returnErrorPage(formWithErrors, mode),
        formData =>
          addressService.schemeManagersAddress(formData) match {
            case None          =>
              Future.successful(
                Redirect(
                  SchemeManagersAddressPage.nextPageRecovery(Some(SchemeManagersAddressPage.recoveryModeReturnUrl))
                )
              )
            case Some(address) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SchemeManagersAddressPage, address))
                savedForLater  <- userAnswersService.setExternalUserAnswers(
                                    updatedAnswers,
                                    request.sessionData.schemeInformation.srnNumber
                                  )
              } yield savedForLater match {
                case Right(Done) => Redirect(SchemeManagersAddressPage.nextPage(mode, updatedAnswers))
                case Left(err)   => onFailureRedirect(err)
              }
          }
      )
  }
}
