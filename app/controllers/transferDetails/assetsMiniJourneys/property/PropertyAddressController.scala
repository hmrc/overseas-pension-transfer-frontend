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

package controllers.transferDetails.assetsMiniJourneys.property

import config.FrontendAppConfig
import controllers.actions._
import forms.transferDetails.assetsMiniJourneys.property.{PropertyAddressFormDataTrait, PropertyAddressFormProvider}
import models.assets.TypeOfAsset.Property
import models.requests.DisplayRequest
import models.{AmendCheckMode, Mode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.property.PropertyAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import queries.assets.AssetsRecordVersionQuery
import queries.{TransferDetailsRecordVersionQuery, TypeOfAssetsRecordVersionQuery}
import services.{AddressService, CountryService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import views.html.transferDetails.assetsMiniJourneys.property.PropertyAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class PropertyAddressController @Inject() (
    override val messagesApi: MessagesApi,
    userAnswersService: UserAnswersService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: PropertyAddressFormProvider,
    countryService: CountryService,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyAddressView
  )(implicit ec: ExecutionContext,
    appConfig: FrontendAppConfig
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val form                   = formProvider(appConfig.accessibilityAddressChanges)
      val preparedForm           = request.userAnswers.get(PropertyAddressPage(index)) match {
        case None          => form
        case Some(address) => form.fill(PropertyAddressFormDataTrait.fromDomain(address))
      }
      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

      Ok(view(preparedForm, countrySelectViewModel, mode, index))
  }

  def renderErrorPage(
      formWithErrors: Form[PropertyAddressFormDataTrait],
      mode: Mode,
      index: Int
    )(implicit displayRequest: DisplayRequest[_]
    ): Future[Result] = {
    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
    Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode, index)))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      val form      = formProvider(appConfig.accessibilityAddressChanges)
      val boundForm = form.bindFromRequest()

      boundForm.fold(
        formWithErrors => renderErrorPage(formWithErrors, mode, index),
        formData =>
          addressService.propertyAddress(formData) match {
            case None                                                                                         =>
              Future.successful(
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              )
            case Some(addressToSave) if addressToSave.country.code != "GB" && addressToSave.postcode.nonEmpty =>
              // TODO Update with a more accurate message, and maybe have it focus the country field rather than postcode
              renderErrorPage(boundForm.withError("postcode", "membersLastUKAddress.error.postcode.incorrect"), mode, index)
            case Some(addressToSave)                                                                          =>
              def setAnswers(): Try[UserAnswers] =
                if (mode == AmendCheckMode) {
                  for {
                    addCashAmount                      <- request.userAnswers.set(PropertyAddressPage(index), addressToSave)
                    removeTransferDetailsRecordVersion <- addCashAmount.remove(TransferDetailsRecordVersionQuery)
                    removeTypeOfAssetsRecordVersion    <- removeTransferDetailsRecordVersion.remove(TypeOfAssetsRecordVersionQuery)
                    removeAssetRecordVersion           <- removeTypeOfAssetsRecordVersion.remove(AssetsRecordVersionQuery(index, Property))
                  } yield removeAssetRecordVersion
                } else {
                  request.userAnswers.set(PropertyAddressPage(index), addressToSave)
                }

              for {
                updatedAnswers <- Future.fromTry(setAnswers())
                _              <- userAnswersService.setExternalUserAnswers(updatedAnswers)
              } yield Redirect(PropertyAddressPage(index).nextPage(mode, updatedAnswers))
          }
      )
  }
}
