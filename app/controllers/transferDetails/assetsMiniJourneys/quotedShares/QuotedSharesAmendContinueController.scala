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

package controllers.transferDetails.assetsMiniJourneys.quotedShares

import controllers.actions._
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import forms.transferDetails.assetsMiniJourney.quotedShares.QuotedSharesAmendContinueFormProvider
import models.{CheckMode, Mode, NormalMode, QuotedSharesEntry, TypeOfAsset, UserAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourney.quotedShares.QuotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourney.quotedShares.QuotedSharesAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QuotedSharesAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: QuotedSharesAmendContinueFormProvider,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    view: QuotedSharesAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      def renderView(answers: UserAnswers): Result = {
        val shares = QuotedSharesAmendContinueSummary.rows(answers)
        Ok(view(form, shares, mode))
      }

      mode match {
        case CheckMode =>
          transferDetailsService
            .setAssetCompleted(request.userAnswers, TypeOfAsset.QuotedShares, completed = false)
            .map {
              case Some(updatedAnswers) => renderView(updatedAnswers)
              case None                 =>
                logger.warn("Failed to reset completion flag for QuotedShares in CheckMode")
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }

        case NormalMode =>
          Future.successful(renderView(request.userAnswers))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = QuotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          if (continue) {
            val nextIndex = transferDetailsService.assetCount[QuotedSharesEntry](request.userAnswers)
            Future.successful(Redirect(AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(NormalMode, nextIndex)))
          } else {
            transferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.QuotedShares, completed = true).map {
              case Some(updatedAnswers) =>
                transferDetailsService.getNextAssetRoute(updatedAnswers) match {
                  case Some(route) => Redirect(route)
                  case None        => Redirect(routes.TransferDetailsCYAController.onPageLoad())
                }
              case None                 =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
          }
        }
      )
  }
}
