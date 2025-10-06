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
import controllers.helpers.ErrorHandling
import forms.transferDetails.IsTransferCashOnlyFormProvider
import models.{Mode, SessionData}
import models.TaskCategory.TransferDetails
import models.assets.TypeOfAsset
import org.apache.pekko.Done
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import pages.transferDetails.{AmountOfTransferPage, IsTransferCashOnlyPage, TypeOfAssetPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Writes._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, TaskService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.IsTransferCashOnlyView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class IsTransferCashOnlyController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    userAnswersService: UserAnswersService,
    formProvider: IsTransferCashOnlyFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: IsTransferCashOnlyView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(IsTransferCashOnlyPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value =>
          for {
            ua1           <- Future.fromTry(updateCashOnlyAnswers(request.userAnswers, value))
            ua2           <- Future.fromTry(TaskService.setInProgressInCheckMode(mode, ua1, taskCategory = TransferDetails))
            savedForLater <- userAnswersService.setExternalUserAnswers(ua2)
            sd            <- Future.fromTry(updateCashOnlySession(request.sessionData, value))
            _             <- sessionRepository.set(sd)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(IsTransferCashOnlyPage.nextPage(mode, ua2))
              case Left(err)   => onFailureRedirect(err)
            }
          }
      )
  }

  private def updateCashOnlySession(sessionData: SessionData, isCashOnly: Boolean): Try[SessionData] =
    if (isCashOnly) {
      for {
        sd  <- AssetsMiniJourneyService.clearAllAssetCompletionFlags(sessionData)
        sd1 <- sd.set(TypeOfAssetPage, Seq[TypeOfAsset](TypeOfAsset.Cash))
      } yield sd1
    } else {
      Success(sessionData)
    }

  private def updateCashOnlyAnswers(userAnswers: models.UserAnswers, isCashOnly: Boolean): Try[models.UserAnswers] = {
    if (isCashOnly) {
      val netAmount = userAnswers.get(AmountOfTransferPage).getOrElse(BigDecimal(0))
      for {
        ua1 <- AssetsMiniJourneyService.removeAllAssetEntriesExceptCash(userAnswers)
        ua2 <- ua1.set(CashAmountInTransferPage, netAmount)
        ua3 <- ua2.set(TypeOfAssetPage, Seq[TypeOfAsset](TypeOfAsset.Cash))
        ua4 <- ua3.set(IsTransferCashOnlyPage, isCashOnly)
      } yield ua4
    } else {
      for {
        ua1 <- userAnswers.remove(CashAmountInTransferPage)
        ua2 <- ua1.remove(TypeOfAssetPage)
        ua3 <- ua2.set(IsTransferCashOnlyPage, isCashOnly)
      } yield ua3
    }
  }
}
