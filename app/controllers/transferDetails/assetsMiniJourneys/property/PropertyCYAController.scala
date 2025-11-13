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
import controllers.actions.{DataRetrievalAction, IdentifierAction, SchemeDataAction}
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import handlers.AssetThresholdHandler
import models.assets.TypeOfAsset
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import org.apache.pekko.Done
import pages.transferDetails.assetsMiniJourneys.property.PropertyCYAPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.assets.PropertyQuery
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
    schemeData: SchemeDataAction,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  private val actions = (identify andThen schemeData andThen getData)

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(PropertySummary.rows(mode, request.userAnswers, index))

    Ok(view(list, mode, index))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = actions.async { implicit request =>
    val updatedUserAnswers = AssetThresholdHandler.handle(request.userAnswers, TypeOfAsset.Property, userSelection = None)
    for {
      saved <- userAnswersService.setExternalUserAnswers(updatedUserAnswers)
    } yield {
      saved match {
        case Right(Done) =>
          Redirect(PropertyCYAPage(index).nextPage(mode, request.userAnswers))
        case _           =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
