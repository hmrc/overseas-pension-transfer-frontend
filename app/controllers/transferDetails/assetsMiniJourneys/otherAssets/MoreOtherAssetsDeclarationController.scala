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
import forms.transferDetails.assetsMiniJourneys.otherAssets.MoreOtherAssetsDeclarationFormProvider
import models.assets.TypeOfAsset
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.otherAssets.MoreOtherAssetsDeclarationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, MoreAssetCompletionService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.otherAssets.OtherAssetsAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.otherAssets.MoreOtherAssetsDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoreOtherAssetsDeclarationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: MoreOtherAssetsDeclarationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MoreOtherAssetsDeclarationView,
    moreAssetCompletionService: MoreAssetCompletionService,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(MoreOtherAssetsDeclarationPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      def renderView(answers: UserAnswers): Result = {
        val assets = OtherAssetsAmendContinueSummary.rows(mode, answers)
        Ok(view(preparedForm, assets, mode))
      }

      mode match {
        case CheckMode | FinalCheckMode | AmendCheckMode =>
          for {
            updatedSessson <- Future.fromTry(
                                AssetsMiniJourneyService.setAssetCompleted(
                                  request.sessionData,
                                  TypeOfAsset.Other,
                                  completed = false
                                )
                              )
            _              <- sessionRepository.set(updatedSessson)

          } yield renderView(request.userAnswers)

        case NormalMode =>
          Future.successful(renderView(request.userAnswers))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val assets = OtherAssetsAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, assets, mode)))
        },
        continue => {
          for {
            userAnswers            <- Future.fromTry(request.userAnswers.set(MoreOtherAssetsDeclarationPage, continue))
            _                      <- userAnswersService.setExternalUserAnswers(userAnswers)
            sessionAfterCompletion <-
              moreAssetCompletionService.completeAsset(userAnswers, request.sessionData, TypeOfAsset.Other, completed = true, Some(continue))
          } yield Redirect(MoreOtherAssetsDeclarationPage.nextPageWith(mode, userAnswers, sessionAfterCompletion))
        }
      )
    }
}
