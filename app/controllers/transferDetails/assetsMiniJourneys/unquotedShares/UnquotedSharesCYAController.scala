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

package controllers.transferDetails.assetsMiniJourneys.unquotedShares

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, DisplayAction, IdentifierAction}
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import models.{NormalMode, UserAnswers}
import org.apache.pekko.Done
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.assets.UnquotedSharesQuery
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourney.unquotedShares.UnquotedSharesSummary
import viewmodels.govuk.summarylist._
import views.html.transferDetails.assetsMiniJourney.unquotedShares.UnquotedSharesCYAView

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class UnquotedSharesCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: UnquotedSharesCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils with Logging {

  private val actions = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(UnquotedSharesSummary.rows(request.userAnswers, index))

    Ok(view(list, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    for {
      minimalUserAnswers <- Future.fromTry(UserAnswers.buildMinimal(request.userAnswers, UnquotedSharesQuery))
      saved              <- userAnswersService.setExternalUserAnswers(minimalUserAnswers)
    } yield {
      saved match {
        case Right(Done) =>
          Redirect(AssetsMiniJourneysRoutes.UnquotedSharesAmendContinueController.onPageLoad(mode = NormalMode))
        case _           =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
