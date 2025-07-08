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
import forms.transferDetails.QuotedSharesConfirmRemovalFormProvider
import models.TypeOfAsset
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.QuotedSharesConfirmRemovalView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QuotedSharesConfirmRemovalController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: QuotedSharesConfirmRemovalFormProvider,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    view: QuotedSharesConfirmRemovalView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form    = formProvider()
  private val actions = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    Ok(view(form, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions.async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
      value => {
        val redirect = Redirect(routes.AdditionalQuotedSharesController.onPageLoad())
        if (value) {
          transferDetailsService.doAssetRemoval(request.userAnswers, index, TypeOfAsset.QuotedShares).map(_ => redirect)
        } else {
          Future.successful(redirect)
        }
      }
    )
  }
}
