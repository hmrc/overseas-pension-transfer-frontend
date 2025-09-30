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
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueFormProvider
import models.assets.{QuotedSharesMiniJourney, TypeOfAsset}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinuePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.AssetsMiniJourneyService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class QuotedSharesAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: QuotedSharesAmendContinueFormProvider,
    sessionRepository: SessionRepository,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: QuotedSharesMiniJourney.type,
    view: QuotedSharesAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(QuotedSharesAmendContinuePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode  =>
          for {
            updatedAnswers <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.userAnswers, TypeOfAsset.QuotedShares, completed = true))
          } yield {
            val shares = QuotedSharesAmendContinueSummary.rows(updatedAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode =>
          val shares = QuotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = QuotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          for {
            ua1 <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.userAnswers, TypeOfAsset.QuotedShares, completed = true))
            ua2 <- Future.fromTry(ua1.set(QuotedSharesAmendContinuePage, continue))
          } yield {
            val nextIndex = AssetsMiniJourneyService.assetCount(miniJourney, request.userAnswers)
            Redirect(QuotedSharesAmendContinuePage.nextPageWith(mode, ua2, nextIndex))
          }
        }
      )
  }
}
