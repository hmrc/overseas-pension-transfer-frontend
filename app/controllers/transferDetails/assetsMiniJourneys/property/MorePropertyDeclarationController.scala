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

import controllers.actions._
import forms.transferDetails.assetsMiniJourneys.property.{MorePropertyDeclarationFormProvider, MorePropertyDeclarationPage}
import models.assets.TypeOfAsset
import models.{CheckMode, Mode, NormalMode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.{MoreAssetCompletionService, TransferDetailsService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.property.MorePropertyDeclarationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class MorePropertyDeclarationController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: MorePropertyDeclarationFormProvider,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    view: MorePropertyDeclarationView,
    moreAssetCompletionService: MoreAssetCompletionService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      val preparedForm = request.userAnswers.get(MorePropertyDeclarationPage) match {
        case Some(value) => form.fill(value)
        case None        => form
      }

      def renderView(answers: UserAnswers): Result = {
        val assets = PropertyAmendContinueSummary.rows(answers)
        Ok(view(preparedForm, assets, mode))
      }

      mode match {
        case CheckMode =>
          for {
            updatedAnswers <- Future.fromTry(
                                transferDetailsService.setAssetCompleted(
                                  request.userAnswers,
                                  TypeOfAsset.Property,
                                  completed = false
                                )
                              )
            _              <- sessionRepository.set(updatedAnswers)
          } yield renderView(updatedAnswers)

        case NormalMode =>
          Future.successful(renderView(request.userAnswers))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val assets = PropertyAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, assets, mode)))
        },
        continue => {
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MorePropertyDeclarationPage, continue))
            _              <- moreAssetCompletionService.completeAsset(updatedAnswers, TypeOfAsset.Property, completed = true, Some(continue))
          } yield Redirect(controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad())
        }
      )
    }
}
