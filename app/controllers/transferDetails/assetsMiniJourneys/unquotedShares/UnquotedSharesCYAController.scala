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

import services.UserAnswersService
import utils.AppUtils
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCYAView
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import com.google.inject.Inject
import handlers.AssetThresholdHandler
import controllers.actions.DataRetrievalAction
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesSummary
import models.Mode
import org.apache.pekko.Done
import viewmodels.govuk.summarylist._
import play.api.Logging
import models.assets.TypeOfAsset
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCYAPage
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class UnquotedSharesCYAController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  userAnswersService: UserAnswersService,
  val controllerComponents: MessagesControllerComponents,
  view: UnquotedSharesCYAView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with AppUtils
    with Logging {

  private val actions = identify andThen schemeData andThen getData

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(UnquotedSharesSummary.rows(mode, request.userAnswers, index))

    Ok(view(list, mode, index))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = actions.async { implicit request =>
    val updatedUserAnswers =
      AssetThresholdHandler.handle(request.userAnswers, TypeOfAsset.UnquotedShares, userSelection = None)
    for {
      saved <-
        userAnswersService.setExternalUserAnswers(updatedUserAnswers, request.sessionData.schemeInformation.srnNumber)
    } yield saved match {
      case Right(Done) =>
        Redirect(UnquotedSharesCYAPage(index).nextPage(mode, request.userAnswers))
      case _           =>
        Redirect(UnquotedSharesCYAPage(index).nextPageRecovery())
    }
  }
}
