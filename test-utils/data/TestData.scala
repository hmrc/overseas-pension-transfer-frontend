/*
 * Copyright 2026 HM Revenue & Customs
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

package data

import models.address.{Country, QROPSAddress, SchemeManagersAddress}
import models.authentication.*
import models.email.{EmailToSendRequest, SubmissionConfirmation}
import models.{AllTransfersItem, DashboardData, IndividualDetails, MinimalDetails, PensionSchemeDetails, PensionSchemeResponse, PersonName, PstrNumber, QtNumber, QtStatus, SchemeManagerType, SessionData, SrnNumber, TransferNumber, UserAnswers}
import org.scalatest.TryValues
import pages.memberDetails.MemberNamePage
import pages.qropsDetails.{QROPSAddressPage, QROPSCountryPage, QROPSNamePage, QROPSReferencePage}
import pages.qropsSchemeManagerDetails.{SchemeManagerTypePage, SchemeManagersAddressPage, SchemeManagersContactPage, SchemeManagersEmailPage, SchemeManagersNamePage}
import play.api.libs.json.{JsObject, Json, Writes}
import queries.{DateSubmittedQuery, PensionSchemeDetailsQuery, QtNumberQuery, Settable}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import utils.DateTimeFormats.localDateTimeFormatter

import java.time.{Clock, Instant, LocalDate, ZoneId}
import java.util.UUID
import scala.util.Random

trait TestData extends TryValues {
  protected final val minYear: Int  = 1901
  private val clockMillis: Long     = 1718118467838L
  val clock: Clock                  = Clock.fixed(Instant.ofEpochMilli(clockMillis), ZoneId.of("UTC"))
  protected val maxCurrency: Double = config.Constants.MaxCurrency
  protected val minCurrency: Double = config.Constants.MinCurrency

  val now: Instant     = Instant.now(clock)
  val today: LocalDate = LocalDate.now(clock)

  val testMemberName: PersonName = PersonName("User", "McUser")

  val testQtNumber: QtNumber = QtNumber("QT123456")

  val testInternalId = "internal-test-id"

  val testTransferNumber: TransferNumber = TransferNumber(UUID.randomUUID().toString)
  val pstr: PstrNumber                   = PstrNumber("12345678AB")

  val psaId: PsaId = PsaId("A2100005")

  val psaUser: PsaUser = PsaUser(psaId, testInternalId, Individual)

  val pspId: PspId = PspId("21000005")

  val pspUser: PspUser = PspUser(pspId, testInternalId, Individual)

  val srn: SrnNumber     = SrnNumber("S2400000040")
  val schemeName: String = "Open Scheme Overview API Test"

  val psr: PensionSchemeResponse = PensionSchemeResponse(pstr, schemeName)

  val schemeDetails: PensionSchemeDetails = PensionSchemeDetails(
    srn,
    pstr,
    schemeName
  )

  def userAnswersMemberName: UserAnswers = emptyUserAnswers.set(MemberNamePage, testMemberName).success.value

  def sessionDataMemberName: SessionData = emptySessionData.set(MemberNamePage, testMemberName).success.value

  def sessionDataMemberNameQtNumber: SessionData = sessionDataMemberName.set(QtNumberQuery, testQtNumber).success.value

  def userAnswersMemberNameQtNumber: UserAnswers = userAnswersMemberName.set(QtNumberQuery, testQtNumber).success.value

  def sessionDataMemberNameQtNumberTransferSubmitted: SessionData =
    sessionDataMemberNameQtNumber.set(DateSubmittedQuery, now).success.value

  def userAnswersMemberNameQtNumberTransferSubmitted: UserAnswers =
    userAnswersMemberNameQtNumber.set(DateSubmittedQuery, now).success.value

  protected val testUserAnswerData: JsObject = userAnswersMemberName.data

  protected val testDashboardData: DashboardData =
    DashboardData(testInternalId, Json.obj(), now).set(PensionSchemeDetailsQuery, schemeDetails).success.value

  protected val testSessionData = SessionData(
    testInternalId,
    testTransferNumber,
    schemeDetails,
    psaUser,
    Json.obj(),
    now
  )

  val formattedTestDateTransferSubmitted: String =
    localDateTimeFormatter.withZone(ZoneId.systemDefault()).format(now)

  def emptyUserAnswers: UserAnswers = UserAnswers(testTransferNumber, pstr, Json.obj(), now)

  val emptySessionData: SessionData = SessionData(
    "sessionId",
    testTransferNumber,
    PensionSchemeDetails(
      SrnNumber("1234567890"),
      PstrNumber("12345678AB"),
      "SchemeName"
    ),
    PsaUser(
      PsaId("A123456"),
      "internalId",
      Individual
    ),
    Json.obj(),
    now
  )

  private def generateNino(prefix: String = "AA"): String = {
    val num    = Random.nextInt(1000000)
    val suffix = "C"
    val nino   = f"$prefix$num%06d$suffix"
    nino
  }

  val testNino: String = generateNino()

  val transferItem: AllTransfersItem = AllTransfersItem(
    transferId = testTransferNumber,
    qtVersion = Some("v1"),
    qtStatus = Some(QtStatus.InProgress),
    nino = Some(testNino),
    memberFirstName = Some("John"),
    memberSurname = Some("Doe"),
    qtDate = Some(today),
    lastUpdated = Some(now),
    pstrNumber = Some(pstr),
    submissionDate = None
  )

  val individualSubmitterDetails: IndividualDetails = IndividualDetails("David", None, "Frost")

  val minimalDetailsIndividual: MinimalDetails =
    MinimalDetails("d.frost@test.com", false, None, Some(individualSubmitterDetails), false, false)

  extension (ua: UserAnswers)
    def setValue[A](page: Settable[A], value: A)(implicit writes: Writes[A]): UserAnswers =
      ua.set(page, value).success.value

  val validMemberDetails: JsObject = Json.obj(
    "memberDetails" -> Json.obj(
      "name"                   -> Json.obj("firstName" -> "Firstname", "lastName" -> "Lastname"),
      "nino"                   -> "AA000000A",
      "dateOfBirth"            -> "1993-11-11",
      "principalResAddDetails" -> Json.obj(
        "addressLine1" -> "line1",
        "addressLine2" -> "line2",
        "country"      -> Json.obj("code" -> "GB", "name" -> "United Kingdom")
      ),
      "memUkResident"          -> true
    )
  )

  val validTransferDetails: JsObject = Json.obj(
    "allowanceBeforeTransfer" -> 1000.25,
    "transferAmount"          -> 2000.88,
    "isTransferTaxable"       -> true,
    "paymentTaxableOverseas"  -> true,
    "whyTaxable"              -> "transferExceedsOTCAllowance",
    "whyTaxableOT"            -> "transferExceedsOTCAllowance",
    "applicableExclusion"     -> Set("occupational"),
    "amountTaxDeducted"       -> 100.33,
    "transferMinusTax"        -> 1900.99,
    "dateMemberTransferred"   -> "2025-04-01",
    "cashOnlyTransfer"        -> true,
    "typeOfAsset"             -> Seq("cashAssets"),
    "cashValue"               -> 2000.88
  )

  val ukCountry = Country("GB", "United Kingdom")

  val qropsAddressTest = QROPSAddress(
    "line1",
    "line2",
    None,
    None,
    None,
    ukCountry
  )

  val managerAddress = SchemeManagersAddress(
    "line1",
    "line2",
    None,
    None,
    None,
    ukCountry
  )

  val canSubmitUserAnswers: UserAnswers =
    emptyUserAnswers
      .copy(data = validMemberDetails ++ Json.obj("transferDetails" -> validTransferDetails))
      .setValue(QROPSNamePage, "Test QROPS")
      .setValue(QROPSReferencePage, "Q123456")
      .setValue(QROPSAddressPage, qropsAddressTest)
      .setValue(QROPSCountryPage, ukCountry)
      .setValue(SchemeManagerTypePage, SchemeManagerType.Individual)
      .setValue(SchemeManagersNamePage, PersonName("John", "Doe"))
      .setValue(SchemeManagersAddressPage, managerAddress)
      .setValue(SchemeManagersEmailPage, "test@gmail.co.uk")
      .setValue(SchemeManagersContactPage, "0121456789")

  val emailRequest: EmailToSendRequest =
    EmailToSendRequest(
      to = List("test.user@example.com"),
      templateId = "submitted-confirmation-template",
      parameters = SubmissionConfirmation(
        qtReference = "QT123456",
        memberName = "Foo Bar",
        submitter = "David Frost",
        submissionDate = "3 October 2025 at 3:00pm",
        pensionSchemeName = "Smith Harper Pension Scheme"
      )
    )
}
