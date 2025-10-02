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
import forms.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueFormProvider
import models.assets.{PropertyMiniJourney, TypeOfAsset}
import models.{CheckMode, Mode, NormalMode}
import pages.transferDetails.assetsMiniJourneys.property.PropertyAmendContinuePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PropertyAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: PropertyAmendContinueFormProvider,
    userAnswersService: UserAnswersService,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: PropertyMiniJourney.type,
    view: PropertyAmendContinueView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(PropertyAmendContinuePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode  =>
          for {
            updatedAnswers <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.userAnswers, TypeOfAsset.Property, completed = true))
            _              <- userAnswersService.setExternalUserAnswers(updatedAnswers)
          } yield {
            val shares = PropertyAmendContinueSummary.rows(updatedAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode =>
          val shares = PropertyAmendContinueSummary.rows(request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = PropertyAmendContinueSummary.rows(request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          for {
            ua  <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.userAnswers, TypeOfAsset.Property, completed = true))
            ua1 <- Future.fromTry(ua.set(PropertyAmendContinuePage, continue))
            _   <- userAnswersService.setExternalUserAnswers(ua1)
          } yield {
            val nextIndex = AssetsMiniJourneyService.assetCount(miniJourney, request.userAnswers)
            Redirect(PropertyAmendContinuePage.nextPageWith(mode, ua1, nextIndex))
          }
        }
      )
  }
}
