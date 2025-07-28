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

package controllers.transferDetails.assetsMiniJourneys.unquotedShares

import controllers.actions._
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import controllers.transferDetails.routes
import forms.transferDetails.assetsMiniJourney.unquotedShares.UnquotedSharesAmendContinueFormProvider
import models.{NormalMode, TypeOfAsset}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourney.unquotedShares.UnquotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourney.unquotedShares.UnquotedSharesAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnquotedSharesAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: UnquotedSharesAmendContinueFormProvider,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    view: UnquotedSharesAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val shares = UnquotedSharesAmendContinueSummary.rows(request.userAnswers)
      Ok(view(form, shares))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = UnquotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares)))
        },
        continue => {
          if (continue) {
            val nextIndex = transferDetailsService.assetCount(request.userAnswers, TypeOfAsset.UnquotedShares)
            Future.successful(Redirect(AssetsMiniJourneysRoutes.UnquotedSharesCompanyNameController.onPageLoad(NormalMode, nextIndex)))
          } else {
            transferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.UnquotedShares).map {
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
