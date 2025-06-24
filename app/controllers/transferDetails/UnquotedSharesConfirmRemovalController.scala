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
import forms.transferDetails.UnquotedSharesConfirmRemovalFormProvider
import models.{Mode, NormalMode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.UnquotedSharesConfirmRemovalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnquotedSharesConfirmRemovalController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: UnquotedSharesConfirmRemovalFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: UnquotedSharesConfirmRemovalView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form    = formProvider()
  private val actions = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(): Action[AnyContent] = actions { implicit request =>
    Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = actions.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
      value => {
        val redirect = Redirect(routes.AdditionalUnquotedShareController.onPageLoad(NormalMode))
        if (value) {
          doRemoval(request.userAnswers).map(_ => redirect)
        } else {
          Future.successful(redirect)
        }
      }
    )
  }

  private def doRemoval(userAnswers: UserAnswers): Future[Unit] = {
    // TODO removal process
    Future.successful(())
  }
}
