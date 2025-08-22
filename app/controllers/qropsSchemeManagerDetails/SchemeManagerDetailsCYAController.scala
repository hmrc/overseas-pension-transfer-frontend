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

package controllers.qropsSchemeManagerDetails

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, DisplayAction, IdentifierAction}
import models.{CheckMode, NormalMode}
import pages.qropsSchemeManagerDetails.SchemeManagerDetailsSummaryPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.govuk.summarylist._
import views.html.qropsSchemeManagerDetails.SchemeManagerDetailsCYAView

class SchemeManagerDetailsCYAController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    val controllerComponents: MessagesControllerComponents,
    view: SchemeManagerDetailsCYAView
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val list = SummaryListViewModel(SchemeManagerDetailsSummary.rows(CheckMode, request.userAnswers))

      Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      {
        Redirect(SchemeManagerDetailsSummaryPage.nextPage(NormalMode, request.userAnswers))
      }
  }
}
