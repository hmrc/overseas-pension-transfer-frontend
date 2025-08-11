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

package controllers.transferDetails

import controllers.actions._
import forms.transferDetails.IsTransferCashOnlyFormProvider
import models.Mode
import models.assets.TypeOfAsset
import org.apache.pekko.Done
import pages.transferDetails.{AmountOfTransferPage, CashAmountInTransferPage, IsTransferCashOnlyPage, TypeOfAssetPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Writes._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.IsTransferCashOnlyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class IsTransferCashOnlyController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: IsTransferCashOnlyFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: IsTransferCashOnlyView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(IsTransferCashOnlyPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            baseAnswers    <- Future.fromTry(request.userAnswers.set(IsTransferCashOnlyPage, value)) // set cash only to true
            updatedAnswers <- if (value) {
                                val netAmount  = request.userAnswers.get(AmountOfTransferPage).getOrElse(BigDecimal(0))
                                val updatedTry = for {
                                  ua1 <- request.userAnswers.set(CashAmountInTransferPage, netAmount)
                                  ua2 <- ua1.set(TypeOfAssetPage, Set[TypeOfAsset](TypeOfAsset.Cash))
                                  ua3 <- ua2.set(IsTransferCashOnlyPage, value)
                                } yield ua3
                                Future.fromTry(updatedTry)
                              } else {
                                val updatedTry = for {
                                  ua1 <- request.userAnswers.remove(CashAmountInTransferPage)
                                  ua2 <- ua1.remove(TypeOfAssetPage)
                                  ua3 <- ua2.set(IsTransferCashOnlyPage, value)
                                } yield ua3
                                Future.fromTry(updatedTry)
                              }
            _              <- sessionRepository.set(updatedAnswers)
            savedForLater  <- userAnswersService.setExternalUserAnswers(updatedAnswers)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(IsTransferCashOnlyPage.nextPage(mode, updatedAnswers))
              case _           => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
      )
  }
}
