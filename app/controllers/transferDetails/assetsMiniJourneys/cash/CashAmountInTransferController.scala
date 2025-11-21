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

package controllers.transferDetails.assetsMiniJourneys.cash

import controllers.actions._
import forms.transferDetails.CashAmountInTransferFormProvider
import models.{AmendCheckMode, Mode, UserAnswers}
import models.assets.TypeOfAsset
import pages.transferDetails.assetsMiniJourneys.cash.CashAmountInTransferPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{TransferDetailsRecordVersionQuery, TypeOfAssetsRecordVersionQuery}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.CashAmountInTransferView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CashAmountInTransferController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: CashAmountInTransferFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: CashAmountInTransferView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(CashAmountInTransferPage) match {
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
        value => {
          def setAnswers(): Try[UserAnswers] =
            if (mode == AmendCheckMode) {
              for {
                addCashAmount                      <- request.userAnswers.set(CashAmountInTransferPage, value)
                removeTransferDetailsRecordVersion <- addCashAmount.remove(TransferDetailsRecordVersionQuery)
                removeTypeOfAssetsRecordVersion    <- removeTransferDetailsRecordVersion.remove(TypeOfAssetsRecordVersionQuery)
              } yield removeTypeOfAssetsRecordVersion
            } else {
              request.userAnswers.set(CashAmountInTransferPage, value)
            }

          for {
            updatedAnswers <- Future.fromTry(setAnswers())
            updatedSession <- Future.fromTry(
                                AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.Cash, completed = true)
                              )
            _              <- sessionRepository.set(updatedSession)
            _              <- userAnswersService.setExternalUserAnswers(updatedAnswers)
          } yield Redirect(CashAmountInTransferPage.nextPageWith(mode, updatedAnswers, updatedSession))
        }
      )
  }
}
