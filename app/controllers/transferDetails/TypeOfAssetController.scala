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

package controllers.transferDetails

import controllers.actions._
import forms.transferDetails.TypeOfAssetFormProvider
import models.Mode
import navigators.TypeOfAssetNavigator
import pages.transferDetails.TypeOfAssetPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.AssetsMiniJourneyService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.TypeOfAssetView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TypeOfAssetController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: TypeOfAssetFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: TypeOfAssetView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(TypeOfAssetPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        selectedAssets => {
          val orderedAssets = selectedAssets.toSeq.sorted
          for {
            setAssetsUA               <- Future.fromTry(request.userAnswers.set(TypeOfAssetPage, selectedAssets))
            removePrevSetAssetFlagsUA <- Future.fromTry(AssetsMiniJourneyService.clearAllAssetCompletionFlags(setAssetsUA))
            setAssetsCompletedUA      <- Future.fromTry(AssetsMiniJourneyService.setSelectedAssetsIncomplete(removePrevSetAssetFlagsUA, orderedAssets))
            _                         <- sessionRepository.set(setAssetsCompletedUA)
          } yield TypeOfAssetNavigator.getNextAssetRoute(setAssetsCompletedUA) match {
            case Some(route) => Redirect(route)
            case None        => Redirect(routes.TransferDetailsCYAController.onPageLoad())
          }
        }
      )
    }
}
