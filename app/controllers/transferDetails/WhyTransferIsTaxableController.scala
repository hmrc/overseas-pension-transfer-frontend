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
import forms.transferDetails.WhyTransferIsTaxableFormProvider
import models.TaskCategory.TransferDetails
import models.{AmendCheckMode, Mode, UserAnswers}
import org.apache.pekko.Done
import pages.transferDetails.WhyTransferIsTaxablePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.TransferDetailsRecordVersionQuery
import services.{TaskService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.WhyTransferIsTaxableView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class WhyTransferIsTaxableController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: WhyTransferIsTaxableFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: WhyTransferIsTaxableView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with ErrorHandling {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(WhyTransferIsTaxablePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          def setAnswers(): Try[UserAnswers] =
            if (mode == AmendCheckMode) {
              request.userAnswers.set(WhyTransferIsTaxablePage, value) flatMap {
                answers =>
                  answers.remove(TransferDetailsRecordVersionQuery)
              }
            } else {
              request.userAnswers.set(WhyTransferIsTaxablePage, value)
            }

          for {
            ua1           <- Future.fromTry(setAnswers())
            ua2           <- Future.fromTry(TaskService.setInProgressInCheckMode(mode, ua1, taskCategory = TransferDetails))
            savedForLater <- userAnswersService.setExternalUserAnswers(ua2)
          } yield {
            savedForLater match {
              case Right(Done) => Redirect(WhyTransferIsTaxablePage.nextPage(mode, ua2))
              case Left(err)   => onFailureRedirect(err)
            }
          }
        }
      )
  }
}
