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
import forms.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueFormProvider
import models.assets.{QuotedSharesMiniJourney, TypeOfAsset}
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueAssetPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{TransferDetailsRecordVersionQuery, TypeOfAssetsRecordVersionQuery}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.quotedShares.QuotedSharesAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class QuotedSharesAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: QuotedSharesAmendContinueFormProvider,
    userAnswersService: UserAnswersService,
    sessionRepository: SessionRepository,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: QuotedSharesMiniJourney.type,
    view: QuotedSharesAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(QuotedSharesAmendContinueAssetPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode | FinalCheckMode | AmendCheckMode =>
          for {
            updatedSession <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.QuotedShares, completed = false))
            _              <- sessionRepository.set(updatedSession)
          } yield {
            val shares = QuotedSharesAmendContinueSummary.rows(mode, request.userAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode                                  =>
          val shares = QuotedSharesAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = QuotedSharesAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          def setAnswers(): Try[UserAnswers] =
            if (mode == AmendCheckMode && continue) {
              for {
                addContinue                        <- request.userAnswers.set(QuotedSharesAmendContinueAssetPage, continue)
                removeTransferDetailsRecordVersion <- addContinue.remove(TransferDetailsRecordVersionQuery)
                removeTypeOfAssetsRecordVersion    <- removeTransferDetailsRecordVersion.remove(TypeOfAssetsRecordVersionQuery)
              } yield removeTypeOfAssetsRecordVersion
            } else {
              request.userAnswers.set(QuotedSharesAmendContinueAssetPage, continue)
            }

          for {
            sd  <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.QuotedShares, completed = true))
            _   <- sessionRepository.set(sd)
            ua1 <- Future.fromTry(setAnswers())
            _   <- userAnswersService.setExternalUserAnswers(ua1)
          } yield {
            val nextIndex = AssetsMiniJourneyService.assetCount(miniJourney, request.userAnswers)
            Redirect(QuotedSharesAmendContinueAssetPage.nextPageWith(mode, ua1, (sd, nextIndex)))
          }
        }
      )
  }
}
