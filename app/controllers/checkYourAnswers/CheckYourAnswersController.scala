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

package controllers.checkYourAnswers

import views.html.checkYourAnswers.CheckYourAnswersView
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import com.google.inject.Inject
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import pages.checkYourAnswers.CheckYourAnswersPage
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import controllers.actions.DataRetrievalAction
import controllers.actions.IdentifierAction
import controllers.actions.SchemeDataAction
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import models.FinalCheckMode
import models.NormalMode
import viewmodels.govuk.summarylist._
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  val controllerComponents: MessagesControllerComponents,
  view: CheckYourAnswersView
) extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val memberDetailsSummaryList        = SummaryListViewModel(MemberDetailsSummary.rows(FinalCheckMode, request.userAnswers))
    val transferDetailsSummaryList      =
      SummaryListViewModel(TransferDetailsSummary.rows(FinalCheckMode, request.userAnswers))
    val qropsDetailsSummaryList         = SummaryListViewModel(QROPSDetailsSummary.rows(FinalCheckMode, request.userAnswers))
    val schemeManagerDetailsSummaryList =
      SummaryListViewModel(SchemeManagerDetailsSummary.rows(FinalCheckMode, request.userAnswers))

    Ok(
      view(
        memberDetailsSummaryList,
        transferDetailsSummaryList,
        qropsDetailsSummaryList,
        schemeManagerDetailsSummaryList
      )
    )
  }

  def onSubmit(): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    Redirect(CheckYourAnswersPage.nextPage(NormalMode, request.userAnswers))
  }
}
