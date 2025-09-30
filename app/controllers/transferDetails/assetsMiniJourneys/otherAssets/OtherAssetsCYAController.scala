/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import handlers.AssetThresholdHandler
import models.assets.TypeOfAsset
import models.{CheckMode, NormalMode, UserAnswers}
import org.apache.pekko.Done
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.assets.OtherAssetsQuery
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsSummary
import viewmodels.govuk.summarylist._
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsCYAView

import scala.concurrent.{ExecutionContext, Future}

class OtherAssetsCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    userAnswersService: UserAnswersService,
    assetThresholdHandler: AssetThresholdHandler,
    val controllerComponents: MessagesControllerComponents,
    view: OtherAssetsCYAView,
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  private val actions = (identify andThen schemeData andThen getData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(OtherAssetsSummary.rows(CheckMode, request.userAnswers, index))

    Ok(view(list, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    for {
      minimalUserAnswers <- Future.fromTry(UserAnswers.buildMinimal(request.userAnswers, OtherAssetsQuery))
      updatedUserAnswers  = assetThresholdHandler.handle(minimalUserAnswers, TypeOfAsset.Other, userSelection = None)
      saved              <- userAnswersService.setExternalUserAnswers(updatedUserAnswers)

    } yield {
      saved match {
        case Right(Done) =>
          val otherAssetsCount = assetThresholdHandler.getAssetCount(minimalUserAnswers, TypeOfAsset.Other)
          if (otherAssetsCount >= 5) {
            Redirect(
              controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.MoreOtherAssetsDeclarationController.onPageLoad(mode = NormalMode)
            )
          } else {
            Redirect(
              AssetsMiniJourneysRoutes.OtherAssetsAmendContinueController.onPageLoad(mode = NormalMode)
            )
          }
        case _           =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
