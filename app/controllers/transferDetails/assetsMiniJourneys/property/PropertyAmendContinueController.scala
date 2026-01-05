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
import models.{AmendCheckMode, CheckMode, FinalCheckMode, Mode, NormalMode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueAssetPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.{TransferDetailsRecordVersionQuery, TypeOfAssetsRecordVersionQuery}
import repositories.SessionRepository
import services.{AssetsMiniJourneyService, UserAnswersService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.checkAnswers.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueSummary
import views.html.transferDetails.assetsMiniJourneys.property.PropertyAmendContinueView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class PropertyAmendContinueController @Inject() (
    override val messagesApi: MessagesApi,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: PropertyAmendContinueFormProvider,
    sessionRepository: SessionRepository,
    val controllerComponents: MessagesControllerComponents,
    miniJourney: PropertyMiniJourney.type,
    view: PropertyAmendContinueView,
    userAnswersService: UserAnswersService
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen schemeData andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.get(PropertyAmendContinueAssetPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      mode match {
        case CheckMode | FinalCheckMode | AmendCheckMode =>
          for {
            updatedSession <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.Property, completed = false))
            _              <- sessionRepository.set(updatedSession)
          } yield {
            val shares = PropertyAmendContinueSummary.rows(mode, request.userAnswers)
            Ok(view(preparedForm, shares, mode))
          }
        case NormalMode                                  =>
          val shares = PropertyAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(Ok(view(preparedForm, shares, mode)))
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val shares = PropertyAmendContinueSummary.rows(mode, request.userAnswers)
          Future.successful(BadRequest(view(formWithErrors, shares, mode)))
        },
        continue => {
          def setAnswers(): Try[UserAnswers] =
            if (mode == AmendCheckMode && continue) {
              for {
                addContinue                        <- request.userAnswers.set(PropertyAmendContinueAssetPage, continue)
                removeTransferDetailsRecordVersion <- addContinue.remove(TransferDetailsRecordVersionQuery)
                removeTypeOfAssetsRecordVersion    <- removeTransferDetailsRecordVersion.remove(TypeOfAssetsRecordVersionQuery)
              } yield removeTypeOfAssetsRecordVersion
            } else {
              request.userAnswers.set(PropertyAmendContinueAssetPage, continue)
            }

          for {
            sd  <- Future.fromTry(AssetsMiniJourneyService.setAssetCompleted(request.sessionData, TypeOfAsset.Property, completed = true))
            _   <- sessionRepository.set(sd)
            ua1 <- Future.fromTry(setAnswers())
            _   <- userAnswersService.setExternalUserAnswers(ua1)
          } yield {
            val nextIndex = AssetsMiniJourneyService.assetCount(miniJourney, request.userAnswers)
            Redirect(PropertyAmendContinueAssetPage.nextPageWith(mode, ua1, (sd, nextIndex)))
          }
        }
      )
  }
}
