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

import services.CountryService
import services.UserAnswersService
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import controllers.actions._
import models.address.Country
import controllers.helpers.ErrorHandling
import models.Mode
import pages.qropsDetails.QROPSCountryPage
import org.apache.pekko.Done
import views.html.qropsDetails.QROPSCountryView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import forms.qropsDetails.QROPSCountryFormProvider
import play.api.data.Form

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class QROPSCountryController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: QROPSCountryFormProvider,
  countryService: CountryService,
  val controllerComponents: MessagesControllerComponents,
  view: QROPSCountryView,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ErrorHandling {

  val form: Form[String] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.get(QROPSCountryPage) match {
      case None          => form
      case Some(country) => form.fill(country.code)
    }

    val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
    Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
            Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
          },
          countryCode => {
            val maybeCountry: Option[Country] =
              countryService.findByCode(countryCode)
            maybeCountry match {
              case None          =>
                Future.successful(
                  Redirect(
                    QROPSCountryPage.nextPageRecovery(
                      Some(QROPSCountryPage.recoveryModeReturnUrl)
                    )
                  )
                )
              case Some(country) =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(QROPSCountryPage, country))
                  savedForLater  <-
                    userAnswersService
                      .setExternalUserAnswers(updatedAnswers, request.sessionData.schemeInformation.srnNumber)
                } yield savedForLater match {
                  case Right(Done) => Redirect(QROPSCountryPage.nextPage(mode, updatedAnswers))
                  case Left(err)   => onFailureRedirect(err)
                }
            }
          }
        )
  }
}
