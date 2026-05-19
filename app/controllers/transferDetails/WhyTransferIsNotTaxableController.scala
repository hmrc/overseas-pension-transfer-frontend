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
import views.html.transferDetails.WhyTransferIsNotTaxableView
import forms.transferDetails.WhyTransferIsNotTaxableFormProvider
import controllers.helpers.ErrorHandling
import models._
import org.apache.pekko.Done
import play.api.data.Form
import pages.transferDetails.WhyTransferIsNotTaxablePage
import services.UserAnswersService
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

import javax.inject.Inject

class WhyTransferIsNotTaxableController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: WhyTransferIsNotTaxableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: WhyTransferIsNotTaxableView,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ErrorHandling {

  val form: Form[Set[WhyTransferIsNotTaxable]] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.get(WhyTransferIsNotTaxablePage) match {
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
                request.userAnswers.set(WhyTransferIsNotTaxablePage, value) flatMap { answers =>
                  answers.remove(TransferDetailsRecordVersionQuery)
                }
              } else {
                request.userAnswers.set(WhyTransferIsNotTaxablePage, value)
              }

            for {
              updatedAnswers <- Future.fromTry(setAnswers())
              savedForLater  <-
                userAnswersService
                  .setExternalUserAnswers(updatedAnswers, request.sessionData.schemeInformation.srnNumber)
            } yield savedForLater match {
              case Right(Done) => Redirect(WhyTransferIsNotTaxablePage.nextPage(mode, updatedAnswers))
              case Left(err)   => onFailureRedirect(err)
            }
          }
        )
  }
}
