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
import models.address.{Countries, PropertyAddress}
import models.authentication.{PsaId, PsaUser, PspId, PspUser}
import models.requests.DisplayRequest
import models.{PensionSchemeDetails, PersonName, PstrNumber, QtNumber, SessionData, SrnNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.memberDetails.MemberNamePage
import pages.transferDetails.assetsMiniJourneys.otherAssets.{OtherAssetsDescriptionPage, OtherAssetsValuePage}
import pages.transferDetails.assetsMiniJourneys.property.{PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import pages.transferDetails.assetsMiniJourneys.quotedShares.{QuotedSharesClassPage, QuotedSharesCompanyNamePage, QuotedSharesNumberPage, QuotedSharesValuePage}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.{
  UnquotedSharesClassPage,
  UnquotedSharesCompanyNamePage,
  UnquotedSharesNumberPage,
  UnquotedSharesValuePage
}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import queries.{DateSubmittedQuery, QtNumberQuery}

import java.time.{Instant, LocalDateTime}
import java.time.format.{DateTimeFormatter, FormatStyle}

trait SpecBase
    extends Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val testMemberName: PersonName = PersonName("User", "McUser")

  val testQtNumber: QtNumber = QtNumber("QT123456")

  val userAnswersId: String = "id"

  val pstr: PstrNumber = PstrNumber("12345678AB")

  val psaId: PsaId = PsaId("A123456")

  val psaUser: PsaUser = PsaUser(psaId, internalId = userAnswersId)

  val pspId = PspId("X7654321")

  val pspUser: PspUser = PspUser(pspId, internalId = userAnswersId)

  val schemeDetails = PensionSchemeDetails(
    SrnNumber("S1234567"),
    PstrNumber("12345678AB"),
    "Scheme Name"
  )

  val testDateTransferSubmitted: LocalDateTime   = LocalDateTime.now
  val formattedTestDateTransferSubmitted: String = testDateTransferSubmitted.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, pstr)

  val emptySessionData: SessionData = SessionData(
    "sessionId",
    "id",
    PensionSchemeDetails(
      SrnNumber("1234567890"),
      PstrNumber("12345678AB"),
      "SchemeName"
    ),
    PsaUser(
      PsaId("A123456"),
      "internalId",
      None
    ),
    Json.obj()
  )

  def userAnswersMemberName: UserAnswers = emptyUserAnswers.set(MemberNamePage, testMemberName).success.value

  def sessionDataQtNumber: SessionData = emptySessionData.set(QtNumberQuery, testQtNumber).success.value

  def userAnswersMemberNameQtNumber: UserAnswers = userAnswersMemberName.set(QtNumberQuery, testQtNumber).success.value

  def sessionDataQtNumberTransferSubmitted: SessionData =
    sessionDataQtNumber.set(DateSubmittedQuery, testDateTransferSubmitted).success.value

  def userAnswersMemberNameQtNumberTransferSubmitted: UserAnswers =
    userAnswersMemberNameQtNumber.set(DateSubmittedQuery, testDateTransferSubmitted).success.value

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(
      userAnswers: UserAnswers = UserAnswers("id", PstrNumber("12345678AB")),
      sessionData: SessionData = sessionDataQtNumber
    ): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, sessionData)),
        bind[MarkInProgressOnEntryAction].to[FakeMarkInProgressAction],
        bind[SchemeDataAction].to[FakeSchemeDataAction]
      )

  def fakeDisplayRequest[A](fakeRequest: FakeRequest[A], userAnswers: UserAnswers = emptyUserAnswers): DisplayRequest[A] =
    DisplayRequest(
      request               = fakeRequest,
      authenticatedUser     = psaUser.updatePensionSchemeDetails(schemeDetails),
      userAnswers           = userAnswers,
      sessionData           = emptySessionData,
      memberName            = testMemberName.fullName,
      qtNumber              = testQtNumber,
      dateTransferSubmitted = formattedTestDateTransferSubmitted
    )

  implicit val testDisplayRequest: DisplayRequest[_] =
    DisplayRequest(
      request               = FakeRequest(),
      authenticatedUser     = psaUser.updatePensionSchemeDetails(schemeDetails),
      userAnswers           = emptyUserAnswers,
      sessionData           = emptySessionData,
      memberName            = testMemberName.fullName,
      qtNumber              = testQtNumber,
      dateTransferSubmitted = formattedTestDateTransferSubmitted
    )

  def userAnswersWithAssets(assetsCount: Int = 1): UserAnswers = {
    (0 until assetsCount).foldLeft(
      emptyUserAnswers
        .set(QtNumberQuery, testQtNumber)
        .success
        .value
    ) { (ua, idx) =>
      val updatedUa =
        ua.set(
          PropertyAddressPage(idx),
          PropertyAddress(
            addressLine1 = s"${idx + 1} Property${idx + 1}",
            addressLine2 = s"Test address line ${idx + 1}",
            None,
            None,
            country      = Countries.UK,
            None
          )
        ).success.value
          .set(PropertyValuePage(idx), BigDecimal(10000 + idx * 1000)) // incremental property values
          .success.value
          .set(PropertyDescriptionPage(idx), s"Description-${idx + 1}")
          .success.value

      // Add other assets
      val withOtherAssets =
        updatedUa.set(
          OtherAssetsDescriptionPage(idx),
          s"OtherAsset-${idx + 1}"
        ).success.value
          .set(OtherAssetsValuePage(idx), BigDecimal(200 + idx * 50))
          .success.value

      // Add unquoted shares
      val withUnquotedShares =
        withOtherAssets.set(
          UnquotedSharesCompanyNamePage(idx),
          s"UnquotedCompany-${idx + 1}"
        ).success.value
          .set(UnquotedSharesValuePage(idx), BigDecimal(300 + idx * 75))
          .success.value
          .set(UnquotedSharesNumberPage(idx), 400 + idx * 100)
          .success.value
          .set(UnquotedSharesClassPage(idx), "A")
          .success.value

      // Add quoted shares
      val withQuotedShares =
        withUnquotedShares.set(
          QuotedSharesCompanyNamePage(idx),
          s"QuotedCompany-${idx + 1}"
        ).success.value
          .set(QuotedSharesValuePage(idx), BigDecimal(500 + idx * 125))
          .success.value
          .set(QuotedSharesNumberPage(idx), 600 + idx * 150)
          .success.value
          .set(QuotedSharesClassPage(idx), "B")
          .success.value

      withQuotedShares
    }
  }
}
