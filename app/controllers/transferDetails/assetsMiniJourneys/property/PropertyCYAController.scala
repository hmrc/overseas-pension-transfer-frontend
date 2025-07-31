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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertySummary
import viewmodels.govuk.summarylist._
import views.html.transferDetails.assetsMiniJourneys.property.PropertyCYAView

import scala.concurrent.ExecutionContext

class PropertyCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  private val actions = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(PropertySummary.rows(request.userAnswers, index))

    Ok(view(list, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions { implicit request =>
    Redirect(controllers.routes.IndexController.onPageLoad())
//    for {
//      minimalUserAnswers <- Future.fromTry(UserAnswers.buildMinimal(request.userAnswers, QuotedSharesQuery))
//      saved              <- userAnswersService.setExternalUserAnswers(minimalUserAnswers)
//    } yield {
//      saved match {
//        case Right(Done) =>
//          Redirect(AssetsMiniJourneysRoutes.PropertyAmendContinueController.onPageLoad(mode = NormalMode))
//        case _           =>
//          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
//      }
//    }
  }
}
