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

package controllers.transferDetails.assetsMiniJourneys.quotedShares

import services.UserAnswersService
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import controllers.actions._
import pages.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesNumberPage
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesNumberFormProvider
import models.Mode
import play.api.data.Form
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesNumberView
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class QuotedSharesNumberController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: QuotedSharesNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: QuotedSharesNumberView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[Int] = formProvider()

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(QuotedSharesNumberPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, index))),
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(QuotedSharesNumberPage(index), value))
              _              <- userAnswersService
                                  .setExternalUserAnswers(updatedAnswers, request.sessionData.schemeInformation.srnNumber)

            } yield Redirect(QuotedSharesNumberPage(index).nextPage(mode, updatedAnswers))
        )
  }
}
