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
import forms.transferDetails.AdditionalUnquotedShareFormProvider
import models.{NormalMode, TypeOfAsset}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.AdditionalUnquotedShareSummary
import views.html.transferDetails.AdditionalUnquotedShareView

import uk.gov.hmrc.hmrcfrontend.views.html.components.AddToAList

import views.ViewUtils.title
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewmodels.LegendSize.Large
import models.requests.DisplayRequest
import views.html.components.QTNumber
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.addtoalist.Short

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AdditionalUnquotedShareController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: AdditionalUnquotedShareFormProvider,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    view: AdditionalUnquotedShareView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val shares = AdditionalUnquotedShareSummary.rows(request.userAnswers)
      Ok(view(form, shares))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = AdditionalUnquotedShareSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares)))
        },
        value => {
          val redirectTarget = if (value) {
            val nextIndex = transferDetailsService.assetCount(request.userAnswers, TypeOfAsset.UnquotedShares)
            routes.UnquotedShareCompanyNameController.onPageLoad(NormalMode, nextIndex)
          } else {
            routes.TransferDetailsCYAController.onPageLoad()
          }
          Future.successful(Redirect(redirectTarget))
        }
      )
  }
}
