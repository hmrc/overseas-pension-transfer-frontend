/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.Inject
import controllers.actions.{DataRequiredAction, DataRetrievalAction, DisplayAction, IdentifierAction}
import models.{NormalMode, ShareEntry, ShareType}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.AppUtils
import viewmodels.checkAnswers.transferDetails.UnquotedShareSummary
import viewmodels.govuk.summarylist._
import views.html.transferDetails.UnquotedShareCYAView
import play.api.libs.json._
import repositories.SessionRepository
import services.TransferDetailsService

import scala.concurrent.{ExecutionContext, Future}

class UnquotedShareCYAController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    transferDetailsService: TransferDetailsService,
    val controllerComponents: MessagesControllerComponents,
    view: UnquotedShareCYAView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with AppUtils {

  private val shareType = ShareType.Unquoted
  private val actions   = (identify andThen getData andThen requireData andThen displayData)

  def onPageLoad(): Action[AnyContent] = actions { implicit request =>
    val list = SummaryListViewModel(UnquotedShareSummary.rows(request.userAnswers))

    Ok(view(list))
  }

  def onSubmit(): Action[AnyContent] = actions.async { implicit request =>
    val path = sharesPathForType(shareType)

    transferDetailsService.unquotedShareBuilder(request.userAnswers) match {
      case Some(newUnquotedShare) =>
        val existingUnquotedShares = request.userAnswers.data
          .validate(path.read[List[ShareEntry]])
          .getOrElse(Nil)

        val updatedShares = existingUnquotedShares :+ newUnquotedShare

        val updatedJson = request.userAnswers.data.deepMerge(
          Json.obj("transferDetails" -> Json.obj(shareType.toString -> Json.toJson(updatedShares)))
        )

        val updatedAnswers = request.userAnswers.copy(data = updatedJson)
        val clearedAnswers = transferDetailsService.clearUnquotedShareFields(updatedAnswers)

        for {
          _ <- sessionRepository.set(clearedAnswers)
        } yield Redirect(routes.AdditionalUnquotedShareController.onPageLoad())

      case None =>
        Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
    }
  }
}
