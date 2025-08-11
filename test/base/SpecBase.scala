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

package base

import controllers.actions._
import models.authentication.{PsaId, PsaUser}
import models.requests.DisplayRequest
import models.{PersonName, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.memberDetails.MemberNamePage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import queries.QtNumber

trait SpecBase
    extends Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val memberDetailsJson: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "name"                    -> Json.obj(
        "firstName" -> "Test",
        "lastName"  -> "McTest"
      ),
      "dateOfBirth"             -> "2011-06-05",
      "nino"                    -> "AB123456B",
      "principalResAddDetails"  -> Json.obj(
        "addressLine1" -> "1",
        "addressLine2" -> "Test road",
        "addressLine3" -> "Testville",
        "addressLine4" -> "East Testerly",
        "ukPostCode"   -> "AB1 2CD",
        "country"      -> Json.obj(
          "code" -> "AE",
          "name" -> "United Arab Emirates"
        ),
        "poBoxNumber"  -> "PO321"
      ),
      "memUkResident"           -> false,
      "memEverUkResident"       -> true,
      "lastPrincipalAddDetails" -> Json.obj(
        "addressLine1" -> "Flat 2",
        "addressLine2" -> "7 Other Place",
        "addressLine3" -> "Some District",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        )
      ),
      "dateMemberLeftUk"        -> "2011-06-06"
    )
  )

  val qropsDetailsJson: JsObject = Json.obj(
    "qropsDetails" -> Json.obj(
      "qropsFullName"         -> "Test Scheme",
      "qropsRef"              -> "AB123",
      "receivingQropsAddress" -> Json.obj(
        "addressLine1" -> "2",
        "addressLine2" -> "QROPS Place",
        "addressLine3" -> "QROPS District",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        )
      ),
      "qropsEstablished"      -> Json.obj(
        "code" -> "GB",
        "name" -> "United Kingdom"
      )
    )
  )

  val schemeManagerDetailsJson: JsObject = Json.obj(
    "schemeManagerDetails" -> Json.obj(
      "schemeManagerType"     -> "organisation",
      "schemeManagerAddress"  -> Json.obj(
        "addressLine1" -> "42",
        "addressLine2" -> "Sesame Street",
        "ukPostCode"   -> "ZZ1 1ZZ",
        "country"      -> Json.obj(
          "code" -> "GB",
          "name" -> "United Kingdom"
        )
      ),
      "schemeManagerEmail"    -> "scheme.manager@email.com",
      "schemeManagerPhone"    -> "07777777777",
      "individualContactName" -> Json.obj(
        "firstName" -> "Individual",
        "lastName"  -> "Lastname"
      )
    )
  )

  val transferDetailsJson: JsObject = Json.obj(
    "transferDetails" -> Json.obj(
      "transferAmount"          -> 12345.99,
      "allowanceBeforeTransfer" -> 54321.99,
      "dateMemberTransferred"   -> "2012-12-12",
      "cashOnlyTransfer"        -> false,
      "paymentTaxableOverseas"  -> true,
      "applicableExclusion"     -> "occupational",
      "amountTaxDeducted"       -> 9876543.21,
      "transferMinusTax"        -> 123456.99,
      "typeOfAsset"             -> Seq("cash", "unquotedShares", "other"),
      "moreQuoted"              -> false,
      "moreUnquoted"            -> true,
      "moreProp"                -> false,
      "moreAsset"               -> false,
      "quotedShares"            -> Seq(
        Json.obj(
          "valueOfShares"  -> 1234.99,
          "numberOfShares" -> 54,
          "companyName"    -> "Some Company",
          "classOfShares"  -> "ABC"
        )
      ),
      "unquotedShares"          -> Seq(
        Json.obj(
          "valueOfShares"  -> 99999.99,
          "numberOfShares" -> 12,
          "companyName"    -> "Unquoted",
          "classOfShares"  -> "Class"
        )
      ),
      "propertyAssets"          -> Seq(
        Json.obj(
          "propertyAddress" -> Json.obj(
            "addressLine1" -> "11 Test Street",
            "addressLine2" -> "Test Town",
            "ukPostCode"   -> "ZZ00 0ZZ",
            "country"      -> Json.obj(
              "code" -> "GB",
              "name" -> "United Kingdom"
            )
          ),
          "propValue"       -> 650000.00,
          "propDescription" -> "Allotment in London"
        )
      ),
      "otherAssets"             -> Seq(
        Json.obj(
          "assetValue"       -> 9876.99,
          "assetDescription" -> "Vintage Car"
        )
      )
    )
  )

  val fullUserAnswersExternalJson: JsObject =
    transferDetailsJson.deepMerge(memberDetailsJson).deepMerge(qropsDetailsJson).deepMerge(schemeManagerDetailsJson)

  val testMemberName: PersonName = PersonName("User", "McUser")

  val testQtNumber: String = "QT123456"

  val userAnswersId: String = "id"

  val psaId: PsaId = PsaId("A123456")

  val authenticatedUser: PsaUser = PsaUser(psaId, internalId = userAnswersId)

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def userAnswersMemberName: UserAnswers = emptyUserAnswers.set(MemberNamePage, testMemberName).success.value

  def userAnswersQtNumber: UserAnswers = emptyUserAnswers.set(QtNumber, testQtNumber).success.value

  def userAnswersMemberNameQtNumber: UserAnswers = userAnswersMemberName.set(QtNumber, testQtNumber).success.value

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[DisplayAction].to[FakeDisplayAction]
      )

  def fakeDisplayRequest[A](fakeRequest: FakeRequest[A], userAnswers: UserAnswers = emptyUserAnswers): DisplayRequest[A] =
    DisplayRequest(
      request           = fakeRequest,
      authenticatedUser = authenticatedUser,
      userAnswers       = userAnswers,
      memberName        = testMemberName.fullName,
      qtNumber          = testQtNumber
    )

  implicit val testDisplayRequest: DisplayRequest[_] =
    DisplayRequest(
      request           = FakeRequest(),
      authenticatedUser = authenticatedUser,
      userAnswers       = emptyUserAnswers,
      memberName        = testMemberName.fullName,
      qtNumber          = testQtNumber
    )

}
