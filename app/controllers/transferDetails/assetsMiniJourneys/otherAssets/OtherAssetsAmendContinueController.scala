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

import controllers.actions._
import forms.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueFormProvider
import models.assets.{OtherAssetsMiniJourney, TypeOfAsset}
import models.{CheckMode, Mode, NormalMode}
import pages.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinuePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OtherAssetsAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    isAssociatedCheck: IsAssociatedCheckAction,
    formProvider: OtherAssetsAmendContinueFormProvider,
    sessionRepository: SessionRepository,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: OtherAssetsMiniJourney.type,
    view: OtherAssetsAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen isAssociatedCheck).async { implicit request =>
      val preparedForm = request.userAnswers.get(OtherAssetsAmendContinuePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode  =>
          for {
            updatedAnswers <- Future.fromTry(TransferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.Other, completed = true))
            _              <- sessionRepository.set(updatedAnswers)
          } yield {
            val shares = OtherAssetsAmendContinueSummary.rows(updatedAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode =>
          val shares = OtherAssetsAmendContinueSummary.rows(request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen isAssociatedCheck).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = OtherAssetsAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          for {
            ua1 <- Future.fromTry(TransferDetailsService.setAssetCompleted(request.userAnswers, TypeOfAsset.Other, completed = true))
            ua2 <- Future.fromTry(ua1.set(OtherAssetsAmendContinuePage, continue))
            _   <- sessionRepository.set(ua2)
          } yield {
            val nextIndex = TransferDetailsService.assetCount(miniJourney, request.userAnswers)
            Redirect(OtherAssetsAmendContinuePage.nextPageWith(mode, ua2, nextIndex))
          }
        }
      )
  }
}
