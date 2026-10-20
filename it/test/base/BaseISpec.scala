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

package base

import models.authentication.{PsaId, PsaUser}
import models.email.{EmailToSendRequest, SubmissionConfirmation}
import models.{DashboardData, IndividualDetails, MinimalDetails, PensionSchemeDetails, PersonName, PstrNumber, QtNumber, SessionData, SrnNumber, TransferNumber, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import pages.memberDetails.MemberNamePage
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.{Application, Environment, Mode}
import queries.PensionSchemeDetailsQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import java.time.{Clock, Instant, ZoneId}
import java.util.UUID
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}
import scala.util.Random

trait BaseISpec
    extends AnyWordSpecLike
    with WireMockHelper
    with Matchers
    with OptionValues
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite {

  private val clockMillis: Long = 1718118467838L
  val clock: Clock              = Clock.fixed(Instant.ofEpochMilli(clockMillis), ZoneId.of("UTC"))

  val now: Instant = Instant.now(clock)

  def servicesConfig: Map[String, String] = Map(
    "play.filters.csrf.header.bypassHeaders.Csrf-Token"            -> "nocheck",
    "microservice.services.overseas-pension-transfer-backend.host" -> WireMockHelper.wireMockHost,
    "microservice.services.overseas-pension-transfer-backend.port" -> WireMockHelper.wireMockPort.toString,
    "microservice.services.pensions-scheme.host"                   -> WireMockHelper.wireMockHost,
    "microservice.services.pensions-scheme.port"                   -> WireMockHelper.wireMockPort.toString,
    "microservice.services.auth.host"                              -> WireMockHelper.wireMockHost,
    "microservice.services.auth.port"                              -> WireMockHelper.wireMockPort.toString,
    "microservice.services.pension-administrator.host"             -> WireMockHelper.wireMockHost,
    "microservice.services.pension-administrator.port"             -> WireMockHelper.wireMockPort.toString,
    "microservice.services.email.host"                             -> WireMockHelper.wireMockHost,
    "microservice.services.email.port"                             -> WireMockHelper.wireMockPort.toString
  )

  def generateNino(prefix: String = "AA"): String = {
    val num    = Random.nextInt(1000000)
    val suffix = "C"
    f"$prefix$num%06d$suffix"
  }

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val appRouteContext: String = "/report-transfer-qualifying-recognised-overseas-pension-scheme"

  val testInternalId = "internal-test-id"
  val testPsaId      = PsaId("A2100005")
  val testPsaUser    = PsaUser(testPsaId, testInternalId, AffinityGroup.Individual)
  val testNino       = "AA123456A"

  val srnNumber         = SrnNumber("222222222")
  val schemeName        = "Smith Harper Pension Scheme"
  val pstrNumber        = PstrNumber("24000001IN")
  val testSchemeDetails = PensionSchemeDetails(srnNumber, pstrNumber, schemeName)

  val testTransferId = TransferNumber(UUID.randomUUID().toString)
  val testQtNumber   = QtNumber("QT564321")
  val testMemberName = PersonName("Margaret", "Thompson")

  val testUserAnswers: UserAnswers =
    UserAnswers(testTransferId, pstrNumber, Json.obj(), now).set(MemberNamePage, testMemberName).success.value

  val testUserAnswerData: JsObject = testUserAnswers.data

  val testDashboardData: DashboardData =
    DashboardData(testInternalId, Json.obj(), now).set(PensionSchemeDetailsQuery, testSchemeDetails).success.value

  val testSessionData = SessionData(
    testInternalId,
    testTransferId,
    testSchemeDetails,
    testPsaUser,
    Json.obj(),
    now
  )

  val dashBoardSessionRepository: DashboardSessionRepository = app.injector.instanceOf[DashboardSessionRepository]
  val sessionRepository: SessionRepository                   = app.injector.instanceOf[SessionRepository]

  val authSession: (String, String) = "authToken" -> "Bearer-token-test"

  val minimalDetails: MinimalDetails =
    MinimalDetails(
      email = "test.user@example.com",
      isPsaSuspended = false,
      organisationName = None,
      individualDetails = Some(
        IndividualDetails(
          firstName = "Test",
          middleName = Some("Middle"),
          lastName = "User"
        )
      ),
      rlsFlag = false,
      deceasedFlag = false
    )

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

  override def beforeAll(): Unit = {
    super.beforeAll()
    startServer()
  }

  override def afterAll(): Unit = {
    stopServer()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    resetWireMock()
  }

  override def afterEach(): Unit =
    super.afterEach()

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

}
