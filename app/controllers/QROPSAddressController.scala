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

import controllers.actions._
import forms.{QROPSAddressFormData, QROPSAddressFormProvider}
import models.Mode
import models.address.{Country, QROPSAddress}
import pages.QROPSAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CountryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.CountrySelectViewModel
import views.html.QROPSAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QROPSAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: QROPSAddressFormProvider,
    countryService: CountryService,
    val controllerComponents: MessagesControllerComponents,
    view: QROPSAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils {

  private def form(): Form[QROPSAddressFormData] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers  = request.userAnswers
      val preparedForm = userAnswers.get(QROPSAddressPage) match {
        case None          => form()
        case Some(address) => form().fill(QROPSAddressFormData.fromDomain(QROPSAddress.fromAddress(address)))
      }

      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

      Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form().bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
        },
        formData => {
          val maybeCountry: Option[Country] =
            countryService.find(formData.countryCode)
          maybeCountry match {
            case None          =>
              Future.successful(
                Redirect(
                  QROPSAddressPage.nextPageRecovery(
                    Some(QROPSAddressPage.recoveryModeReturnUrl)
                  )
                )
              )
            case Some(country) =>
              val addressToSave = QROPSAddress(
                addressLine1 = formData.addressLine1,
                addressLine2 = formData.addressLine2,
                addressLine3 = formData.addressLine3,
                addressLine4 = formData.addressLine4,
                addressLine5 = formData.addressLine5,
                country      = country
              )
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(QROPSAddressPage, addressToSave))
                _              <- {
                  logger.info(Json.stringify(updatedAnswers.data))
                  sessionRepository.set(updatedAnswers)
                }
              } yield Redirect(QROPSAddressPage.nextPage(mode, updatedAnswers))
          }
        }
      )
  }
}
