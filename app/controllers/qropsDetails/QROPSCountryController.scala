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
import forms.qropsDetails.QROPSCountryFormProvider
import models.Mode
import models.address.Country
import org.apache.pekko.Done
import pages.memberDetails.MemberIsResidentUKPage
import pages.qropsDetails.QROPSCountryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{CountryService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.CountrySelectViewModel
import views.html.qropsDetails.QROPSCountryView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QROPSCountryController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    formProvider: QROPSCountryFormProvider,
    countryService: CountryService,
    val controllerComponents: MessagesControllerComponents,
    view: QROPSCountryView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(QROPSCountryPage) match {
        case None          => form
        case Some(country) => form.fill(country.code)
      }

      val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
      Ok(view(preparedForm, countrySelectViewModel, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val countrySelectViewModel = CountrySelectViewModel.fromCountries(countryService.countries)
          Future.successful(BadRequest(view(formWithErrors, countrySelectViewModel, mode)))
        },
        countryCode => {
          val maybeCountry: Option[Country] =
            countryService.find(countryCode)
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
                _              <- sessionRepository.set(updatedAnswers)
                savedForLater  <- userAnswersService.setUserAnswers(updatedAnswers)
              } yield {
                savedForLater match {
                  case Right(Done) => Redirect(QROPSCountryPage.nextPage(mode, updatedAnswers))
                  case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
                }
              }
          }
        }
      )
  }

}
