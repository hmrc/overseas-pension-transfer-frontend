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

package controllers.memberDetails

import controllers.actions._
import models.address.NoAddressFound
import pages.memberDetails.MembersLastUkAddressLookupPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.memberDetails.MembersLastUkAddressNotFoundView

import javax.inject.Inject

class MembersLastUkAddressNotFoundController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    val controllerComponents: MessagesControllerComponents,
    view: MembersLastUkAddressNotFoundView
  ) extends FrontendBaseController with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData) { implicit request =>
      request.userAnswers.get(MembersLastUkAddressLookupPage) match {
        case Some(NoAddressFound(searchedPostcode)) =>
          Ok(view(searchedPostcode))
        case _                                      =>
          Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

}
