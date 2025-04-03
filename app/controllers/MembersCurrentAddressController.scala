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
import forms.{MembersCurrentAddressFormData, MembersCurrentAddressFormProvider}

import javax.inject.Inject
import models.Mode
import models.address.{Country, MembersCurrentAddress}
import pages.MembersCurrentAddressPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CountryService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import utils.AppUtils
import views.html.MembersCurrentAddressView

import scala.concurrent.{ExecutionContext, Future}

class MembersCurrentAddressController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: MembersCurrentAddressFormProvider,
    countryService: CountryService,
    val controllerComponents: MessagesControllerComponents,
    view: MembersCurrentAddressView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging with AppUtils {

  private def form(memberName: String)(implicit messages: Messages): Form[MembersCurrentAddressFormData] = formProvider(memberName)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val userAnswers  = request.userAnswers
      val memberName   = memberFullName(request.userAnswers)
      val preparedForm = userAnswers.get(MembersCurrentAddressPage) match {
        case None          => form(memberName)
        case Some(address) => form(memberName).fill(MembersCurrentAddressFormData.fromDomain(MembersCurrentAddress.fromAddress(address)))
      }

      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)

      Ok(view(preparedForm, countrySelectViewModel, memberName, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val memberName = memberFullName(request.userAnswers)
      form(memberName).bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, memberName, mode)))

        },
        formData => {
          val maybeCountry: Option[Country] =
            countryService.find(formData.countryCode)
          maybeCountry match {
            case None          =>
              Future.successful(
                Redirect(
                  MembersCurrentAddressPage.nextPageRecovery(
                    Some(MembersCurrentAddressPage.recoveryModeReturnUrl)
                  )
                )
              )
            case Some(country) =>
              val addressToSave = MembersCurrentAddress(
                addressLine1 = formData.addressLine1,
                addressLine2 = formData.addressLine2,
                addressLine3 = formData.addressLine3,
                addressLine4 = formData.addressLine4,
                country      = country,
                postcode     = formData.postcode,
                poBox        = formData.poBox
              )
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersCurrentAddressPage, addressToSave))
                _              <- {
                  logger.info(Json.stringify(updatedAnswers.data))
                  sessionRepository.set(updatedAnswers)
                }
              } yield Redirect(MembersCurrentAddressPage.nextPage(mode, updatedAnswers))
          }
        }
      )
  }

}
