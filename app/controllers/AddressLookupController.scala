/*
 * Copyright 2023 HM Revenue & Customs
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

package controllers

import config.FrontendAppConfig
import connectors.{AddressLookupConnector, AddressLookupErrorResponse, AddressLookupSuccessResponse, HasAddressLookupConnector}
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}

import javax.inject.Inject
import models.{Address, AddressRecord, RecordSet}
import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.BadRequestException
import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
import uk.gov.hmrc.play.bootstrap.frontend.controller.{FrontendBaseController, FrontendController}

import scala.concurrent.{ExecutionContext, Future}

class AddressLookupController @Inject() (
    override val messagesApi: MessagesApi,
    val addressLookupConnector: AddressLookupConnector,
    identify: IdentifierAction,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    val controllerComponents: MessagesControllerComponents,
    implicit val appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with HasAddressLookupConnector with Logging {

  implicit object AddressOrdering extends Ordering[AddressRecord] {
    def compare(a: AddressRecord, b: AddressRecord): Int = a.id compare b.id
  }

  def addressLookup(postcode: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      implicit val writes: OFormat[RecordSet] = Json.format[RecordSet]
      val validPostcodeCharacters             = "^[A-z0-9 ]*$"
      if (postcode.matches(validPostcodeCharacters)) {
        addressLookupConnector.lookup(postcode) map {
          case AddressLookupErrorResponse(e: BadRequestException) => BadRequest(e.message)
          case AddressLookupErrorResponse(_)                      => InternalServerError
          case AddressLookupSuccessResponse(recordSet)            => Ok(writes.writes(RecordSet(recordSet.addresses.sorted)))
        }
      } else {
        Future.successful(BadRequest("missing or badly-formed postcode parameter"))
      }
  }
}
