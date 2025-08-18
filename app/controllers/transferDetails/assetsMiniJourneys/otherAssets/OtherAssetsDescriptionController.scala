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

package controllers.transferDetails.assetsMiniJourneys.otherAssets

import config.FrontendAppConfig
import controllers.actions._
import forms.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsDescriptionFormProvider
import models.Mode
import pages.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsDescriptionPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsDescriptionView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAssetsDescriptionController @Inject() (
    override val messagesApi: MessagesApi,
    appConfig: FrontendAppConfig,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: OtherAssetsDescriptionFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: OtherAssetsDescriptionView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val fromFinalCYA: Boolean = request.request.headers.get(REFERER).getOrElse("/")
        .endsWith(appConfig.finalCheckAnswersUrl)

      val preparedForm = request.userAnswers.get(OtherAssetsDescriptionPage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index, fromFinalCYA))
  }

  def onSubmit(mode: Mode, index: Int, fromFinalCYA: Boolean): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, index))),
        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(OtherAssetsDescriptionPage(index), value))
            _              <- sessionRepository.set(updatedAnswers)
          } yield Redirect(OtherAssetsDescriptionPage(index).nextPage(mode, updatedAnswers, fromFinalCYA))
      )
  }
}
