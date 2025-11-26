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
import handlers.AssetThresholdHandler
import models.Mode
import models.assets.TypeOfAsset
import org.apache.pekko.Done
import pages.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsSummary
import viewmodels.govuk.summarylist._
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsCYAView

import scala.concurrent.ExecutionContext

class OtherAssetsCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: OtherAssetsCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  private val actions = (identify andThen schemeData andThen getData)

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(OtherAssetsSummary.rows(mode, request.userAnswers, index))

    Ok(view(list, mode, index))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = actions.async { implicit request =>
    val updatedUserAnswers = AssetThresholdHandler.handle(request.userAnswers, TypeOfAsset.Other, userSelection = None)

    for {
      saved <- userAnswersService.setExternalUserAnswers(updatedUserAnswers)
    } yield {
      saved match {
        case Right(Done) =>
          Redirect(OtherAssetsCYAPage(index).nextPage(mode, request.userAnswers))
        case _           =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
