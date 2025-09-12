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

import controllers.actions._
import controllers.helpers.ErrorHandling
import forms.qropsDetails.{QROPSAddressFormData, QROPSAddressFormProvider}
import models.Mode
import org.apache.pekko.Done
import pages.qropsDetails.QROPSAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AddressService, CountryService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.CountrySelectViewModel
import views.html.qropsDetails.QROPSAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QROPSAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    getData: DataRetrievalAction,
    formProvider: QROPSAddressFormProvider,
    countryService: CountryService,
    addressService: AddressService,
    val controllerComponents: MessagesControllerComponents,
    view: QROPSAddressView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
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

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form().bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
        },
        formData =>
          addressService.qropsAddress(formData) match {
            case None          =>
              Future.successful(
                Redirect(QROPSAddressPage.nextPageRecovery(Some(QROPSAddressPage.recoveryModeReturnUrl)))
              )
            case Some(address) =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(QROPSAddressPage, address))
                _              <- sessionRepository.set(updatedAnswers).map(_ => logger.info(Json.stringify(updatedAnswers.data)))
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
