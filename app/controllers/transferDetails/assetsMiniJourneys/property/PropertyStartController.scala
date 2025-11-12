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

package controllers.transferDetails.assetsMiniJourneys.property

import controllers.actions._
import models.{Mode, NormalMode}
import pages.transferDetails.assetsMiniJourneys.property.PropertyStartPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.property.PropertyStartView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PropertyStartController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    view: PropertyStartView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      Ok(view(PropertyStartPage.nextPage(mode = mode, request.userAnswers).url))
  }
}
