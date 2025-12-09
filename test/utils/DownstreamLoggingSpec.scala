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

/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import controllers.helpers.ErrorHandling
import controllers.routes
import models.BackendError
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.typedmap.TypedMap
import play.api.mvc._
import play.api.routing.{HandlerDef, Router}
import play.api.test.Helpers._
import play.api.test._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, RequestId}

import scala.concurrent.Future

class DownstreamLoggingSpec extends AnyWordSpec with Matchers {

  object TestLogging extends DownstreamLogging

  object TestController extends BaseController with ErrorHandling {

    override protected def controllerComponents: ControllerComponents =
      stubControllerComponents()

    def failingAction(err: String): Action[AnyContent] = Action.async { implicit request =>
      Future.successful(onFailureRedirect(err))
    }
  }

  "DownstreamLogging" should {

    "build BackendError with correlation ID from response" in {
      val response = HttpResponse(
        status  = INTERNAL_SERVER_ERROR,
        body    = "boom",
        headers = Map(
          "X-Request-ID" -> Seq("corr-123"),
          "Status"       -> Seq("InternalServerError")
        )
      )

      val err = TestLogging.logBackendError("TestOrigin", response)

      err mustBe BackendError("corr-123", INTERNAL_SERVER_ERROR, "InternalServerError", "TestOrigin", "boom")
      err.message must include("TestOrigin")
      err.message must include("corr-123")
    }

    "fallback to '-' correlation ID if none found" in {
      val response = HttpResponse(
        status  = BAD_REQUEST,
        body    = "bad request",
        headers = Map.empty
      )

      val err = TestLogging.logBackendError("AnotherOrigin", response)

      err.correlationId mustBe "-"
      err.status mustBe BAD_REQUEST
      err.reason mustBe "Unknown"
    }

    "logNonHttpError returns message with correlation ID from HeaderCarrier" in {
      implicit val hc: HeaderCarrier = HeaderCarrier(requestId = Some(RequestId("req-999")))
      val ex                         = new RuntimeException("db down")

      val message = TestLogging.logNonHttpError("DBService", hc, ex)

      message must include("DBService")
      message must include("req-999")
      message must include("db down")
    }

    "logNonHttpError falls back to '-' if no correlation ID" in {
      implicit val hc: HeaderCarrier = HeaderCarrier()
      val ex                         = new IllegalStateException("oops")

      val message = TestLogging.logNonHttpError("NoCorrService", hc, ex)

      message must include("NoCorrService")
      message must include("correlationId=-")
      message must include("oops")
    }
  }

  "ErrorHandling" should {

    "redirect to JourneyRecoveryController when failing" in {
      val request = FakeRequest(GET, "/test-url")

      val result = TestController.failingAction("SomeError")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
    }

    "include controller.method in logs if HandlerDef present" in {
      val handlerDef = HandlerDef(
        classLoader    = getClass.getClassLoader,
        routerPackage  = "router",
        controller     = "TestController",
        method         = "failingAction",
        parameterTypes = Seq.empty,
        verb           = "GET",
        path           = "/test-url"
      )

      val request = FakeRequest(GET, "/test-url")
        .withAttrs(TypedMap(Router.Attrs.HandlerDef -> handlerDef))

      val result = TestController.failingAction("SomeError")(request)

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.JourneyRecoveryController.onPageLoad().url)
    }
  }
}
