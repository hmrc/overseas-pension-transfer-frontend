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

package controllers.transferDetails.assetsMiniJourneys.unquotedShares

import controllers.actions._
import controllers.transferDetails.assetsMiniJourneys.AssetsMiniJourneysRoutes
import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueFormProvider
import models.assets.{TypeOfAsset, UnquotedSharesMiniJourney}
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinuePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class UnquotedSharesAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    sessionRepository: SessionRepository,
    formProvider: UnquotedSharesAmendContinueFormProvider,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: UnquotedSharesMiniJourney.type,
    view: UnquotedSharesAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(UnquotedSharesAmendContinuePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode  =>
          for {
            updatedAnswers <- Future.fromTry(TransferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.UnquotedShares, completed = true))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            val shares = UnquotedSharesAmendContinueSummary.rows(updatedAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode =>
          val shares = UnquotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = UnquotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          for {
            ua1 <- Future.fromTry(TransferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.UnquotedShares, completed = true))
            ua2 <- Future.fromTry(ua1.set(UnquotedSharesAmendContinuePage, continue))
            _   <- sessionRepository.set(ua2)
          } yield {
            val nextIndex = TransferDetailsService.assetCount(miniJourney, request.userAnswers)
            Redirect(UnquotedSharesAmendContinuePage.nextPageWith(mode, ua2, nextIndex))
          }
        }
      )
  }
}
