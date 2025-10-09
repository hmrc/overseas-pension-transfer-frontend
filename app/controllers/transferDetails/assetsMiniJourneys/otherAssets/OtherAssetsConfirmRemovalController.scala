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

package controllers.transferDetails.assetsMiniJourneys.otherAssets

import controllers.actions._
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsConfirmRemovalFormProvider
import handlers.AssetThresholdHandler
import models.NormalMode
import models.assets.{OtherAssetsMiniJourney, TypeOfAsset}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{AssetsMiniJourneyService, MoreAssetCompletionService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsConfirmRemovalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAssetsConfirmRemovalController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: OtherAssetsConfirmRemovalFormProvider,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: OtherAssetsMiniJourney.type,
    assetThresholdHandler: AssetThresholdHandler,
    view: OtherAssetsConfirmRemovalView,
    moreAssetCompletionService: MoreAssetCompletionService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form    = formProvider()
  private val actions = (identify andThen schemeData andThen getData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    Ok(view(form, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
      confirmRemoval =>
        if (!confirmRemoval) {
          val otherAssetsCount = assetThresholdHandler.getAssetCount(request.userAnswers, TypeOfAsset.Other)
          val redirectTarget   =
            if (otherAssetsCount >= 5) {

              controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.MoreOtherAssetsDeclarationController.onPageLoad(mode = NormalMode)
            } else {
              AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(mode = NormalMode)
            }

          Future.successful(Redirect(redirectTarget))
        } else {
          (for {
            updatedAnswers <- Future.fromTry(AssetsMiniJourneyService.removeAssetEntry(miniJourney, request.userAnswers, index))
            _              <- userAnswersService.setExternalUserAnswers(updatedAnswers)
            _              <- moreAssetCompletionService.completeAsset(updatedAnswers, request.sessionData, TypeOfAsset.Other, completed = false)
          } yield Redirect(AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(mode = NormalMode)))
            .recover {
              case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        }
    )
  }
}
