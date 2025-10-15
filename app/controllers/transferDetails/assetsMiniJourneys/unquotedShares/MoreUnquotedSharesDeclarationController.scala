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
import forms.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationFormProvider
import models.assets.TypeOfAsset
import models.{CheckMode, FinalCheckMode, Mode, NormalMode, UserAnswers}
import navigators.TypeOfAssetNavigator
import pages.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, MoreAssetCompletionService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.MoreUnquotedSharesDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MoreUnquotedSharesDeclarationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: MoreUnquotedSharesDeclarationFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: MoreUnquotedSharesDeclarationView,
    moreAssetCompletionService: MoreAssetCompletionService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(MoreUnquotedSharesDeclarationPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      def renderView(answers: UserAnswers): Result = {
        val assets = UnquotedSharesAmendContinueSummary.rows(answers)
        Ok(view(preparedForm, assets, mode))
      }

      mode match {
        case CheckMode | FinalCheckMode =>
          for {
            updatedSession <- Future.fromTry(
                                AssetsMiniJourneyService.setAssetCompleted(
                                  request.sessionData,
                                  TypeOfAsset.UnquotedShares,
                                  completed = false
                                )
                              )

            _ <- sessionRepository.set(updatedSession)
          } yield renderView(request.userAnswers)

        case NormalMode =>
          Future.successful(renderView(request.userAnswers))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val assets = UnquotedSharesAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, assets, mode)))
        },
        continue => {
          for {
            userAnswers            <- Future.fromTry(request.userAnswers.set(MoreUnquotedSharesDeclarationPage, continue))
            sessionAfterCompletion <-
              moreAssetCompletionService.completeAsset(userAnswers, request.sessionData, TypeOfAsset.UnquotedShares, completed = true, Some(continue))
          } yield TypeOfAssetNavigator.getNextAssetRoute(sessionAfterCompletion) match {
            case Some(route) => Redirect(route)
            case None        => Redirect(MoreUnquotedSharesDeclarationPage.nextPage(mode, userAnswers))
          }
        }
      )
    }
}
