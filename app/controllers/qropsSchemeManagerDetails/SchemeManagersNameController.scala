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

package controllers.qropsSchemeManagerDetails

import services.UserAnswersService
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.MessagesControllerComponents
import forms.qropsSchemeManagerDetails.SchemeManagersNameFormProvider
import controllers.actions._
import pages.qropsSchemeManagerDetails.SchemeManagersNamePage
import controllers.helpers.ErrorHandling
import models.Mode
import models.PersonName
import org.apache.pekko.Done
import views.html.qropsSchemeManagerDetails.SchemeManagersNameView
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import javax.inject.Inject

class SchemeManagersNameController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: SchemeManagersNameFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: SchemeManagersNameView,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with ErrorHandling {

  val form: Form[PersonName] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) { implicit request =>
    val preparedForm = request.userAnswers.get(SchemeManagersNamePage) match {
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
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(SchemeManagersNamePage, value))
              savedForLater  <-
                userAnswersService
                  .setExternalUserAnswers(updatedAnswers, request.sessionData.schemeInformation.srnNumber)
            } yield savedForLater match {
              case Right(Done) => Redirect(SchemeManagersNamePage.nextPage(mode, updatedAnswers))
              case Left(err)   => onFailureRedirect(err)
            }
        )
  }
}
