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
import models.{Mode, UserAnswers}
import navigators.TypeOfAssetNavigator
import pages.transferDetails.TypeOfAssetPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.TransferDetailsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.TypeOfAssetView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class TypeOfAssetController @Inject() (
    override val messagesApi: MessagesApi,
    sessionRepository: SessionRepository,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    displayData: DisplayAction,
    formProvider: TypeOfAssetFormProvider,
    val controllerComponents: MessagesControllerComponents,
    transferDetailsService: TransferDetailsService,
    typeOfAssetNavigator: TypeOfAssetNavigator,
    view: TypeOfAssetView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData andThen displayData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(TypeOfAssetPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode))
  }

  def onSubmit(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData andThen displayData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode))),
        value => {
          val initialUpdate = request.userAnswers.set(TypeOfAssetPage, value)

          initialUpdate match {
            case Success(updatedAnswersWithTypes) =>
              // For each selected asset type, set it as incomplete in the user answers
              val markAllIncomplete: Future[Option[UserAnswers]] =
                value.foldLeft(Future.successful(Some(updatedAnswersWithTypes): Option[UserAnswers])) {
                  case (maybeAnswersFut, assetType) =>
                    maybeAnswersFut.flatMap {
                      case Some(ua) => transferDetailsService.setAssetCompleted(ua, assetType, completed = false)
                      case None     => Future.successful(None)
                    }
                }

              for {
                maybeFinalAnswers <- markAllIncomplete
                result            <- maybeFinalAnswers match {
                                       case Some(finalAnswers) => sessionRepository.set(finalAnswers)
                                           .map(_ => Redirect(typeOfAssetNavigator.nextPage(TypeOfAssetPage, mode, finalAnswers)))
                                       case None               =>
                                         Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
                                     }
              } yield result

            case Failure(_) =>
              Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad()))
          }
        }
      )
    }
}
