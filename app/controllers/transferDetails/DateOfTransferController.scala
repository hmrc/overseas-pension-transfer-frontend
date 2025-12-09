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
import controllers.helpers.ErrorHandling
import forms.transferDetails.{AmendDateOfTransferFormProvider, DateOfTransferFormProvider}
import models.requests.DisplayRequest
import models.{AmendCheckMode, Mode, UserAnswers}
import org.apache.pekko.Done
import pages.transferDetails.DateOfTransferPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.TransferDetailsRecordVersionQuery
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.DateOfTransferView

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class DateOfTransferController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: DateOfTransferFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: DateOfTransferView,
    userAnswersService: UserAnswersService,
    amendDateOfTransferFormProvider: AmendDateOfTransferFormProvider
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  private def isAmend(mode: Mode): Boolean =
    mode == models.CheckMode || mode == models.AmendCheckMode

  private def prepareForm(form: Form[LocalDate], userAnswers: UserAnswers): Form[LocalDate] =
    userAnswers.get(DateOfTransferPage).fold(form)(form.fill)

  private def handleSubmission(
      form: Form[LocalDate],
      mode: Mode,
      userAnswers: UserAnswers
    )(implicit request: DisplayRequest[_]
    ) = {
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, mode, isAmend(mode)))),
      value => {
        def setAnswers(): Try[UserAnswers] =
          if (mode == AmendCheckMode) {
            userAnswers.set(DateOfTransferPage, value) flatMap {
              answers =>
                answers.remove(TransferDetailsRecordVersionQuery)
            }
          } else {
            userAnswers.set(DateOfTransferPage, value)
          }
        for {
          updatedAnswers <- Future.fromTry(setAnswers())
          savedForLater  <- userAnswersService.setExternalUserAnswers(updatedAnswers)
        } yield savedForLater match {
          case Right(Done) => Redirect(DateOfTransferPage.nextPage(mode, updatedAnswers))
          case Left(err)   => onFailureRedirect(err)
        }
      }
    )
  }

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      if (isAmend(mode)) {
        userAnswersService.getExternalUserAnswers(
          request.userAnswers.id,
          request.userAnswers.pstr,
          models.QtStatus.Submitted,
          Some("001")
        ).map {
          case Right(originalSubmission) =>
            val originalDate = originalSubmission.get(DateOfTransferPage)
              .getOrElse(throw new IllegalStateException("Original submission date has not been found"))
            val form         = amendDateOfTransferFormProvider(originalDate)
            Ok(view(prepareForm(form, request.userAnswers), mode, isAmend = true))
          case _                         =>
            Ok(view(prepareForm(formProvider(), request.userAnswers), mode))
        }
      } else {
        Future.successful(Ok(view(prepareForm(formProvider(), request.userAnswers), mode)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      if (isAmend(mode)) {
        userAnswersService.getExternalUserAnswers(
          request.userAnswers.id,
          request.userAnswers.pstr,
          models.QtStatus.Submitted,
          Some("001")
        ).flatMap {
          case Right(originalSubmission) =>
            val originalDate = originalSubmission.get(DateOfTransferPage)
              .getOrElse(throw new IllegalStateException("Original submission date has not been found"))
            val form         = amendDateOfTransferFormProvider(originalDate)
            handleSubmission(form, mode, request.userAnswers)
          case _                         =>
            handleSubmission(formProvider(), mode, request.userAnswers)
        }
      } else {
        handleSubmission(formProvider(), mode, request.userAnswers)
      }
  }
}
