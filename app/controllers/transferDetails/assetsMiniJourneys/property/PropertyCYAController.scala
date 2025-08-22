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

package controllers.transferDetails.assetsMiniJourneys.property

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, DisplayAction, IdentifierAction}
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import handlers.AssetThresholdHandler
import models.{NormalMode, UserAnswers}
import models.assets.TypeOfAsset
import org.apache.pekko.Done
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.assets.PropertyQuery
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertySummary
import viewmodels.govuk.summarylist._
import views.html.transferDetails.assetsMiniJourneys.property.PropertyCYAView

import scala.concurrent.{ExecutionContext, Future}

class PropertyCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    userAnswersService: UserAnswersService,
    assetThresholdHandler: AssetThresholdHandler,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyCYAView,
    sessionRepository: SessionRepository
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  private val actions = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(PropertySummary.rows(request.userAnswers, index))

    Ok(view(list, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    for {
      minimalUserAnswers <- Future.fromTry(UserAnswers.buildMinimal(request.userAnswers, PropertyQuery))
      updatedUserAnswers  = assetThresholdHandler.handle(minimalUserAnswers, TypeOfAsset.Property, userSelection = None)
      saved              <- userAnswersService.setExternalUserAnswers(updatedUserAnswers)
      _                  <- sessionRepository.set(request.userAnswers)
    } yield {
      saved match {
        case Right(Done) =>
          val propertyCount = assetThresholdHandler.getAssetCount(minimalUserAnswers, TypeOfAsset.Property)
          if (propertyCount >= 5) {
            Redirect(
              controllers.transferDetails.assetsMiniJourneys.property.routes.MorePropertyDeclarationController.onPageLoad(mode = NormalMode)
            )
          } else {
            Redirect(
              AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(mode = NormalMode)
            )
          }
        case _           =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
