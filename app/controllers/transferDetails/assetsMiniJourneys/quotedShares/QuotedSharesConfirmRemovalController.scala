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

import services.AssetsMiniJourneyService
import services.MoreAssetCompletionService
import services.UserAnswersService
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import handlers.AssetThresholdHandler
import controllers.actions._
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesConfirmRemovalFormProvider
import models.assets.QuotedSharesMiniJourney
import models.assets.TypeOfAsset
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.NormalMode
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesConfirmRemovalView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class QuotedSharesConfirmRemovalController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: QuotedSharesConfirmRemovalFormProvider,
  userAnswersService: UserAnswersService,
  val controllerComponents: MessagesControllerComponents,
  miniJourney: QuotedSharesMiniJourney.type,
  view: QuotedSharesConfirmRemovalView,
  moreAssetCompletionService: MoreAssetCompletionService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form    = formProvider()
  private val actions = identify andThen schemeData andThen getData

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    Ok(view(form, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    form
      .bindFromRequest()
      .fold(
        formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
        confirmRemoval =>
          if (!confirmRemoval) {
            val quotedSharesCount = AssetThresholdHandler.getAssetCount(request.userAnswers, TypeOfAsset.QuotedShares)
            val redirectTarget    =
              if (quotedSharesCount >= 5) {

                controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.MoreQuotedSharesDeclarationController
                  .onPageLoad(mode = NormalMode)
              } else {
                AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(mode = NormalMode)
              }

            Future.successful(Redirect(redirectTarget))
          } else {
            (for {
              updatedAnswers <-
                Future.fromTry(AssetsMiniJourneyService.removeAssetEntry(miniJourney, request.userAnswers, index))
              _              <- userAnswersService
                                  .setExternalUserAnswers(updatedAnswers, request.sessionData.schemeInformation.srnNumber)
              _              <- moreAssetCompletionService
                                  .completeAsset(updatedAnswers, request.sessionData, TypeOfAsset.QuotedShares, completed = false)
            } yield Redirect(
              AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(mode = NormalMode)
            ))
              .recover { case _ =>
                Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
              }
          }
      )
  }
}
