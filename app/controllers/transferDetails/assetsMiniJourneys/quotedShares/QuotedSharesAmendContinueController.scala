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
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueFormProvider
import models.assets.{QuotedSharesMiniJourney, TypeOfAsset}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QuotedSharesAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: QuotedSharesAmendContinueFormProvider,
    sessionRepository: SessionRepository,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: QuotedSharesMiniJourney.type,
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
        case CheckMode  =>
          for {
            updatedAnswers <- Future.fromTry(transferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.QuotedShares, completed = false))
            _              <- sessionRepository.set(updatedAnswers)
          } yield renderView(updatedAnswers)
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
            val nextIndex = transferDetailsService.assetCount(miniJourney, request.userAnswers)
            Future.successful(Redirect(AssetsMiniJourneysRoutes.QuotedSharesCompanyNameController.onPageLoad(NormalMode, nextIndex)))
          } else {
            for {
              updatedAnswers <- Future.fromTry(transferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.QuotedShares, completed = true))
              _              <- sessionRepository.set(updatedAnswers)
            } yield transferDetailsService.getNextAssetRoute(updatedAnswers) match {
              case Some(route) => Redirect(route)
              case None        => Redirect(routes.TransferDetailsCYAController.onPageLoad())
            }
          }
        }
      )
  }
}
