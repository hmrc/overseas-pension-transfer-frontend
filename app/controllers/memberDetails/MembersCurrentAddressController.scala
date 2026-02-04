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
import controllers.helpers.ErrorHandling
import forms.memberDetails.{MembersCurrentAddressFormData, MembersCurrentAddressFormProvider}
import models.Mode
import models.requests.DisplayRequest
import org.apache.pekko.Done
import pages.memberDetails.MembersCurrentAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AddressService, CountryService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import views.html.memberDetails.MembersCurrentAddressView
import views.html.memberDetails.MembersCurrentAddressAccessibleView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MembersCurrentAddressController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: MembersCurrentAddressFormProvider,
    userAnswersService: UserAnswersService,
    countryService: CountryService,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: MembersCurrentAddressView,
    accessibleView: MembersCurrentAddressAccessibleView,
    appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging with ErrorHandling {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val form                   = formProvider()
      val preparedForm           = request.userAnswers.get(MembersCurrentAddressPage) match {
        case None          => form
        case Some(address) => form.fill(MembersCurrentAddressFormData.fromDomain(address))
      }
      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
      if (appConfig.accessibilityAddressChanges) {
        Ok(accessibleView(preparedForm, countrySelectViewModel, mode))
      } else {
        Ok(view(preparedForm, countrySelectViewModel, mode))
      }
  }

  def renderErrorPage(formWithErrors: Form[MembersCurrentAddressFormData], mode: Mode)(implicit request: DisplayRequest[_]) = {
    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
    if (appConfig.accessibilityAddressChanges) {
      Future.successful(BadRequest(accessibleView(formWithErrors, countrySelectViewModel, mode)))
    } else {
      Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
    }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      val boundForm = formProvider().bindFromRequest()

      boundForm.fold(
        formWithErrors => renderErrorPage(formWithErrors, mode),
        formData =>
          addressService.membersCurrentAddress(formData) match {
            case None                                                                                                                                  =>
              Future.successful(
                Redirect(MembersCurrentAddressPage.nextPageRecovery(Some(MembersCurrentAddressPage.recoveryModeReturnUrl)))
              )
            case Some(addressToSave) if addressToSave.postcode.nonEmpty && addressToSave.country.code != "GB" && appConfig.accessibilityAddressChanges =>
              renderErrorPage(
                boundForm.withError(
                  "postcode",
                  "membersLastUkAddressLookup.error.pattern"
                ),
                mode
              )
            case Some(addressToSave)                                                                                                                   =>
              for {
                userAnswers   <- Future.fromTry(request.userAnswers.set(MembersCurrentAddressPage, addressToSave))
                savedForLater <- userAnswersService.setExternalUserAnswers(userAnswers)
              } yield {
                savedForLater match {
                  case Right(Done) => Redirect(MembersCurrentAddressPage.nextPage(mode, userAnswers))
                  case Left(err)   => onFailureRedirect(err)
                }

              }
          }
      )
  }
}
