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

package controllers.transferDetails

import controllers.actions._
import forms.transferDetails.AdditionalUnquotedShareFormProvider
import models.{CheckMode, Mode, NormalMode}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.AdditionalUnquotedShareView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalUnquotedShareController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: AdditionalUnquotedShareFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: AdditionalUnquotedShareView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = if (mode == CheckMode) form.fill(false) else form

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val redirectTarget = if (value) {
            // TODO might have to do something for a new UnquotedShares
            routes.UnquotedShareCompanyNameController.onPageLoad(NormalMode)
          } else {
            routes.TransferDetailsCYAController.onPageLoad()
          }
          Future.successful(Redirect(redirectTarget))
        }
      )
  }
}
