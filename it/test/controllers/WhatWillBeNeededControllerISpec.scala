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

package controllers

import base.BaseISpec
import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson, stubFor}
import controllers.actions._
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.{FakeRequest, Helpers, Injecting}
import play.api.{Environment, Mode}
import repositories.SessionRepository

import scala.concurrent.Future

class WhatWillBeNeededControllerISpec extends BaseISpec with Injecting {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val userAnswers: UserAnswers = UserAnswers("userId")

  private lazy val application =
    GuiceApplicationBuilder()
      .in(Environment.simple(mode = Mode.Dev))
      .configure(servicesConfig)
      .overrides(
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(None)),
        bind[DisplayAction].to[FakeDisplayAction]
      )
      .build()

  val controller: WhatWillBeNeededController = application.injector.instanceOf[WhatWillBeNeededController]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "onPageLoad" should {
    "return OK with view and" should {
      "populate session repository with save for later data returned from the backend service" in {

        stubFor(
          get("/save-for-later/userId")
            .willReturn(okJson(
              s"""{
                "referenceId": "userId",
                |"data": {"field": "value"},
                |"lastUpdated": ${userAnswers.lastUpdated}
                |}""".stripMargin))
        )

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val result = controller.onPageLoad()(FakeRequest("GET", "/what-will-be-needed"))

        Helpers.status(result) shouldBe OK

        verify(mockSessionRepository).set(
          UserAnswers(
            "userId",
            JsObject(Map("field" -> JsString("value"))),
            any()
          )
        )
      }

      "populate session repository with userId and blank Json record when no save for later data is returned" in {
        stubFor(
          get("/save-for-later/userId2")
            .willReturn(okJson(
              s"""{
                |"referenceId": "userId2",
                |"data": {},
                |"lastUpdated": ${userAnswers.lastUpdated}
                |}""".stripMargin))
        )

        when(mockSessionRepository.set(any()))
          .thenReturn(Future.successful(true))

        val result = controller.onPageLoad()(FakeRequest("GET", "/what-will-be-needed"))

        Helpers.status(result) shouldBe OK

        verify(mockSessionRepository).set(
          UserAnswers(
            "userId2",
            JsObject.empty,
            any()
          )
        )
      }
    }
  }

}
