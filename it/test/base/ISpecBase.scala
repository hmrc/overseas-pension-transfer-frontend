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

import data.TestData
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Environment, Mode}
import repositories.{DashboardSessionRepository, SessionRepository}
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

trait ISpecBase
    extends AnyWordSpecLike
    with WireMockHelper
    with Matchers
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with GuiceOneServerPerSuite
    with TestData {

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

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .in(Environment.simple(mode = Mode.Dev))
    .configure(servicesConfig)
    .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val appRouteContext: String = "/report-transfer-qualifying-recognised-overseas-pension-scheme"

  protected val dashBoardSessionRepository: DashboardSessionRepository = app.injector.instanceOf[DashboardSessionRepository]
  protected val sessionRepository: SessionRepository                   = app.injector.instanceOf[SessionRepository]

  protected val authSession: (String, String) = "authToken" -> "Bearer-token-test"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    startServer()
  }

  override protected def afterAll(): Unit = {
    stopServer()
    super.afterAll()
  }

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    resetWireMock()
  }
}
