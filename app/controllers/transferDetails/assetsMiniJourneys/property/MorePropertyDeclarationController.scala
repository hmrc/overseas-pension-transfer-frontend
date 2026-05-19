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

package controllers.transferDetails.assetsMiniJourneys.property

import services.AssetsMiniJourneyService
import services.MoreAssetCompletionService
import services.UserAnswersService
import queries.TransferDetailsRecordVersionQuery
import queries.TypeOfAssetsRecordVersionQuery
import forms.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationFormProvider
import play.api.mvc._
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import controllers.actions._
import pages.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationPage
import models.assets.TypeOfAsset
import repositories.SessionRepository
import models._
import play.api.i18n.I18nSupport
import play.api.i18n.MessagesApi
import views.html.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Try

import javax.inject.Inject

class MorePropertyDeclarationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  schemeData: SchemeDataAction,
  formProvider: MorePropertyDeclarationFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: MorePropertyDeclarationView,
  moreAssetCompletionService: MoreAssetCompletionService,
  userAnswersService: UserAnswersService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(MorePropertyDeclarationPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      def renderView(answers: UserAnswers): Result = {
        val assets = PropertyAmendContinueSummary.rows(mode, answers)
        Ok(view(preparedForm, assets, mode))
      }

      mode match {
        case CheckMode | FinalCheckMode | AmendCheckMode =>
          for {
            updatedSession <- Future.fromTry(
                                AssetsMiniJourneyService.setAssetCompleted(
                                  request.sessionData,
                                  TypeOfAsset.Property,
                                  completed = false
                                )
                              )
            _              <- sessionRepository.set(updatedSession)
          } yield renderView(request.userAnswers)

        case NormalMode =>
          Future.successful(renderView(request.userAnswers))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val assets = PropertyAmendContinueSummary.rows(mode, request.userAnswers)
            Future.successful(BadRequest(view(formWithErrors, assets, mode)))
          },
          continue => {
            def setAnswers(): Try[UserAnswers] =
              if (mode == AmendCheckMode) {
                for {
                  addCashAmount                      <- request.userAnswers.set(MorePropertyDeclarationPage, continue)
                  removeTransferDetailsRecordVersion <- addCashAmount.remove(TransferDetailsRecordVersionQuery)
                  removeTypeOfAssetsRecordVersion    <-
                    removeTransferDetailsRecordVersion.remove(TypeOfAssetsRecordVersionQuery)
                } yield removeTypeOfAssetsRecordVersion
              } else {
                request.userAnswers.set(MorePropertyDeclarationPage, continue)
              }

            for {
              userAnswers            <- Future.fromTry(setAnswers())
              _                      <-
                userAnswersService.setExternalUserAnswers(userAnswers, request.sessionData.schemeInformation.srnNumber)
              sessionAfterCompletion <-
                moreAssetCompletionService.completeAsset(
                  userAnswers,
                  request.sessionData,
                  TypeOfAsset.Property,
                  completed = true,
                  Some(continue)
                )
            } yield Redirect(MorePropertyDeclarationPage.nextPageWith(mode, userAnswers, sessionAfterCompletion))
          }
        )
    }
}
