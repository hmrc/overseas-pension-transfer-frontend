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

import queries.TransferDetailsRecordVersionQuery
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import controllers.actions._
import views.html.transferDetails.NetTransferAmountView
import forms.transferDetails.NetTransferAmountFormProvider
import models.AmendCheckMode
import models.Mode
import models.UserAnswers
import play.api.data.Form
import pages.transferDetails.NetTransferAmountPage
import services.UserAnswersService
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

import javax.inject.Inject

class NetTransferAmountController @Inject() (
  override val messagesApi: MessagesApi,
  userAnswersService: UserAnswersService,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: NetTransferAmountFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: NetTransferAmountView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  val form: Form[BigDecimal] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.get(NetTransferAmountPage) match {
      case None        => form
      case Some(value) => form.fill(value)
    }

    Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode))),
          value => {
            def setAnswers(): Try[UserAnswers] =
              if (mode == AmendCheckMode) {
                request.userAnswers.set(NetTransferAmountPage, value) flatMap { answers =>
                  answers.remove(TransferDetailsRecordVersionQuery)
                }
              } else {
                request.userAnswers.set(NetTransferAmountPage, value)
              }

            for {
              updatedAnswers <- Future.fromTry(setAnswers())
              _              <- userAnswersService
                                  .setExternalUserAnswers(updatedAnswers, request.sessionData.schemeInformation.srnNumber)
            } yield Redirect(NetTransferAmountPage.nextPage(mode, updatedAnswers))
          }
        )
  }
}
