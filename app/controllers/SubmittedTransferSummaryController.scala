/*
 * Copyright 2025 HM Revenue & Customs
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

package controllers

import controllers.actions._
import models.{PstrNumber, QtStatus, SessionData}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import services.CollectSubmittedVersionsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.SubmittedTransferSummaryViewModel
import views.html.SubmittedTransferSummaryView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SubmittedTransferSummaryController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    schemeData: SchemeDataAction,
    collectSubmittedVersionsService: CollectSubmittedVersionsService,
    val controllerComponents: MessagesControllerComponents,
    view: SubmittedTransferSummaryView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(qtReference: String, pstr: PstrNumber, qtStatus: QtStatus, versionNumber: String): Action[AnyContent] = (identify andThen schemeData).async {
    implicit request =>
      // Table Builder - create row for each version
      // From version number to 1 make request foreach
      // build row
      // pop on view
      // Merry Boshmas
      // Builder requires PSTR / QTREF / Version

      collectSubmittedVersionsService.collectVersions(qtReference, pstr, qtStatus, versionNumber) map {
        case (maybeDraft, userAnswers) =>
          def createTableRows = SubmittedTransferSummaryViewModel.rows(maybeDraft, userAnswers, versionNumber)
          Ok(view(createTableRows))
      }

  }
}
