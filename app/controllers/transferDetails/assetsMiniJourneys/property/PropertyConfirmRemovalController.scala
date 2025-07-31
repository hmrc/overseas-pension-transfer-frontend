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
import forms.transferDetails.assetsMiniJourneys.property.PropertyConfirmRemovalFormProvider
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{TransferDetailsService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.property.PropertyConfirmRemovalView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class PropertyConfirmRemovalController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    sessionRepository: SessionRepository,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: PropertyConfirmRemovalFormProvider,
    transferDetailsService: TransferDetailsService,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    view: PropertyConfirmRemovalView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  private val form    = formProvider()
  private val actions = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(index: Int): Action[AnyContent] = actions { implicit request =>
    Ok(view(form, index))
  }

  def onSubmit(index: Int): Action[AnyContent] = actions { implicit request =>
    Redirect(controllers.routes.IndexController.onPageLoad())
//    form.bindFromRequest().fold(
//      formWithErrors => Future.successful(BadRequest(view(formWithErrors, index))),
//      confirmRemoval =>
//        if (!confirmRemoval) {
//          Future.successful(Redirect(AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(mode = NormalMode)))
//        } else {
//          (for {
//            updatedAnswers     <- Future.fromTry(transferDetailsService.removeAssetEntry[QuotedSharesEntry](request.userAnswers, index))
//            _                  <- sessionRepository.set(updatedAnswers)
//            minimalUserAnswers <- Future.fromTry(UserAnswers.buildMinimal(updatedAnswers, QuotedSharesQuery))
//            _                  <- userAnswersService.setExternalUserAnswers(minimalUserAnswers)
//          } yield Redirect(AssetsMiniJourneysRoutes.QuotedSharesAmendContinueController.onPageLoad(mode = NormalMode))).recover {
//            case _ => Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
//          }
//        }
//    )
  }
}
