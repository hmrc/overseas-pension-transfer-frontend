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
import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueFormProvider
import models.assets.{TypeOfAsset, UnquotedSharesMiniJourney}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueAssetPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, UserAnswersService}
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
    userAnswersService: UserAnswersService,
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
      val preparedForm = request.userAnswers.get(UnquotedSharesAmendContinueAssetPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode | FinalCheckMode | AmendCheckMode =>
          for {
            updatedSession <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.UnquotedShares, completed = true))
            _              <- sessionRepository.set(updatedSession)
          } yield {
            val shares = UnquotedSharesAmendContinueSummary.rows(mode, request.userAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode                                  =>
          val shares = UnquotedSharesAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = UnquotedSharesAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          for {
            sd  <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.UnquotedShares, completed = true))
            _   <- sessionRepository.set(sd)
            ua1 <- Future.fromTry(request.userAnswers.set(UnquotedSharesAmendContinueAssetPage, continue))
            _   <- userAnswersService.setExternalUserAnswers(ua1)
          } yield {
            val nextIndex = AssetsMiniJourneyService.assetCount(miniJourney, request.userAnswers)
            Redirect(UnquotedSharesAmendContinueAssetPage.nextPageWith(mode, ua1, (sd, nextIndex)))
          }
        }
      )
  }
}
