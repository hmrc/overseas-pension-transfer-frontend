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

package controllers.transferDetails.assetsMiniJourneys.property

import controllers.actions._
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.property.PropertyConfirmRemovalFormProvider
import models.assets.PropertyMiniJourney
import models.{NormalMode, UserAnswers}
import handlers.AssetThresholdHandler
import models.NormalMode
import models.assets.{PropertyMiniJourney, TypeOfAsset}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.{MoreAssetCompletionService, TransferDetailsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.property.PropertyConfirmRemovalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyConfirmRemovalController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    isAssociatedCheck: IsAssociatedCheckAction,
    formProvider: PropertyConfirmRemovalFormProvider,
    miniJourney: PropertyMiniJourney.type,
    assetThresholdHandler: AssetThresholdHandler,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyConfirmRemovalView,
    moreAssetCompletionService: MoreAssetCompletionService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form    = formProvider()
  private val actions = (identify andThen getData andThen isAssociatedCheck)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    Ok(view(form, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
      confirmRemoval =>
        if (!confirmRemoval) {
          val propertyCount  = assetThresholdHandler.getAssetCount(request.userAnswers, TypeOfAsset.Property)
          val redirectTarget =
            if (propertyCount >= 5) {

              controllers.transferDetails.assetsMiniJourneys.property.routes.MorePropertyDeclarationController.onPageLoad(mode = NormalMode)
            } else {
              AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(mode = NormalMode)
            }

          Future.successful(Redirect(redirectTarget))
        } else {
          (for {
            updatedAnswers <- Future.fromTry(TransferDetailsService.removeAssetEntry(miniJourney, request.userAnswers, index))
            _              <- moreAssetCompletionService.completeAsset(updatedAnswers, TypeOfAsset.Property, completed = false)
          } yield Redirect(AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(mode = NormalMode)))
            .recover {
              case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
            }
        }
    )
  }
}
