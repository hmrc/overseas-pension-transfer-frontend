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

package controllers.qropsDetails

import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import com.google.inject.Inject
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import controllers.actions.DataRetrievalAction
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import controllers.helpers.ErrorHandling
import models.CheckMode
import models.NormalMode
import views.html.qropsDetails.QROPSDetailsCYAView
import viewmodels.govuk.summarylist._
import pages.qropsDetails.QROPSDetailsSummaryPage
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext

class QROPSDetailsCYAController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  val controllerComponents: MessagesControllerComponents,
  view: QROPSDetailsCYAView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ErrorHandling {

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val list = SummaryListViewModel(QROPSDetailsSummary.rows(CheckMode, request.userAnswers))

    Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    Redirect(QROPSDetailsSummaryPage.nextPage(NormalMode, request.userAnswers))
  }
}
