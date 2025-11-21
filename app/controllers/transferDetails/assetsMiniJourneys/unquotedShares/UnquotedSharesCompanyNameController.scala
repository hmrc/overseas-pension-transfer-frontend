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
import forms.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCompanyNameFormProvider
import models.assets.TypeOfAsset.UnquotedShares
import models.{AmendCheckMode, Mode, UserAnswers}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCompanyNamePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import queries.assets.AssetsRecordVersionQuery
import queries.{TransferDetailsRecordVersionQuery, TypeOfAssetsRecordVersionQuery}
import services.UserAnswersService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transferDetails.assetsMiniJourneys.unquotedShares.UnquotedSharesCompanyNameView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class UnquotedSharesCompanyNameController @Inject() (
    override val messagesApi: MessagesApi,
    userAnswersService: UserAnswersService,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    schemeData: SchemeDataAction,
    formProvider: UnquotedSharesCompanyNameFormProvider,
    val controllerComponents: MessagesControllerComponents,
    view: UnquotedSharesCompanyNameView
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData) {
    implicit request =>
      val preparedForm = request.userAnswers.get(UnquotedSharesCompanyNamePage(index)) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, mode, index))
  }

  def onSubmit(mode: Mode, index: Int): Action[AnyContent] = (identify andThen schemeData andThen getData).async {
    implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, index))),
        value => {
          def setAnswers(): Try[UserAnswers] =
            if (mode == AmendCheckMode) {
              for {
                addCashAmount                      <- request.userAnswers.set(UnquotedSharesCompanyNamePage(index), value)
                removeTransferDetailsRecordVersion <- addCashAmount.remove(TransferDetailsRecordVersionQuery)
                removeTypeOfAssetsRecordVersion    <- removeTransferDetailsRecordVersion.remove(TypeOfAssetsRecordVersionQuery)
                removeAssetRecordVersion           <- removeTypeOfAssetsRecordVersion.remove(AssetsRecordVersionQuery(index, UnquotedShares))
              } yield removeAssetRecordVersion
            } else {
              request.userAnswers.set(UnquotedSharesCompanyNamePage(index), value)
            }

          for {
            updatedSession <- Future.fromTry(request.userAnswers.set(UnquotedSharesCompanyNamePage(index), value))
            _              <- userAnswersService.setExternalUserAnswers(updatedSession)
          } yield Redirect(UnquotedSharesCompanyNamePage(index).nextPage(mode, updatedSession))
        }
      )
  }
}
