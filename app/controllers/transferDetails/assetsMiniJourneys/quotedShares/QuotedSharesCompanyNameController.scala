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

import controllers.actions._
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesCompanyNameFormProvider
import models.Mode
import pages.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesCompanyNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesCompanyNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QuotedSharesCompanyNameController @Inject() (
    override val messagesApi: MessagesApi,
    userAnswersService: UserAnswersService,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: QuotedSharesCompanyNameFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: QuotedSharesCompanyNameView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(QuotedSharesCompanyNamePage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, index))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(QuotedSharesCompanyNamePage(index), value))
            _              <- userAnswersService.setExternalUserAnswers(updatedAnswers)
          } yield Redirect(QuotedSharesCompanyNamePage(index).nextPage(mode, updatedAnswers))
      )
  }
}
