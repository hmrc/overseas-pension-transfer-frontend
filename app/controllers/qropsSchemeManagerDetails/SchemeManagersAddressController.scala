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

import config.FrontendAppConfig
import controllers.actions._
import controllers.helpers.ErrorHandling
import forms.qropsSchemeManagerDetails.{SchemeManagersAddressFormData, SchemeManagersAddressFormProvider}
import models.Mode
import org.apache.pekko.Done
import pages.qropsSchemeManagerDetails.SchemeManagersAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressService, CountryService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.CountrySelectViewModel
import views.html.qropsSchemeManagerDetails.SchemeManagersAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
  )(implicit ec: ExecutionContext,
    appConfig: FrontendAppConfig
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils with ErrorHandling {

  private def form: Form[SchemeManagersAddressFormData] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val userAnswers  = request.userAnswers
      val preparedForm = userAnswers.get(SchemeManagersAddressPage) match {
        case None          => form
        case Some(address) => form.fill(SchemeManagersAddressFormData.fromDomain(address))
      }

      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

      Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
        },
        formData => {
          addressService.schemeManagersAddress(formData) match {
            case None          =>
              Future.successful(
                Redirect(
                  SchemeManagersAddressPage.nextPageRecovery(
                    Some(SchemeManagersAddressPage.recoveryModeReturnUrl)
                  )
                )
              )
            case Some(address) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(SchemeManagersAddressPage, address))
                savedForLater  <- userAnswersService.setExternalUserAnswers(updatedAnswers)
              } yield {
                savedForLater match {
                  case Right(Done) => Redirect(SchemeManagersAddressPage.nextPage(mode, updatedAnswers))
                  case Left(err)   => onFailureRedirect(err)
                }
              }
          }
        }
      )
  }
}
