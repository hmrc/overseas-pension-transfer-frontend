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

package connectors

import cats.data.EitherT
import org.mockito.Mockito.{reset, times, when}
import org.mockito.{ArgumentMatchers, Mockito}
import org.scalatest.RecoverMethods
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import org.slf4j.{Logger => UnderlyingLogger}
import play.api.Logger
import play.api.test.Injecting

import scala.concurrent.{ExecutionContext, Future}
import play.api.http.Status.*
import uk.gov.hmrc.http.*

class HttpClientResponseSpec extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with BeforeAndAfterEach
    with MockitoSugar
    with Injecting
    with ScalaFutures
    with RecoverMethods {

  private val mockLogger                 = mock[UnderlyingLogger]
  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private lazy val httpClientResponseUsingMockLogger: HttpClientResponse = new HttpClientResponse {
    override protected val logger: Logger = new Logger(mockLogger)
  }

  private val dummyContent = "error message"

  "read" must {
    behave like clientResponseLogger(
      httpClientResponseUsingMockLogger.read,
      infoLevel                  = Set(NOT_FOUND, UNPROCESSABLE_ENTITY),
      warnLevel                  = Set.empty,
      errorLevelWithThrowable    = Set(UNAUTHORIZED),
      errorLevelWithoutThrowable = Set(TOO_MANY_REQUESTS, INTERNAL_SERVER_ERROR)
    )
  }

  "readIgnoreUnauthorised" must {
    behave like clientResponseLogger(
      httpClientResponseUsingMockLogger.readIgnoreUnauthorised,
      infoLevel                  = Set(NOT_FOUND, UNPROCESSABLE_ENTITY),
      warnLevel                  = Set.empty,
      errorLevelWithThrowable    = Set.empty,
      errorLevelWithoutThrowable = Set(TOO_MANY_REQUESTS, INTERNAL_SERVER_ERROR)
    )
  }

  private def clientResponseLogger(
      block: Future[Either[UpstreamErrorResponse, HttpResponse]] => EitherT[Future, UpstreamErrorResponse, HttpResponse],
      infoLevel: Set[Int],
      warnLevel: Set[Int],
      errorLevelWithThrowable: Set[Int],
      errorLevelWithoutThrowable: Set[Int]
    ): Unit = {
    infoLevel.foreach { httpResponseCode =>
      s"log message: INFO level only when response code is $httpResponseCode" in {
        reset(mockLogger)
        when(mockLogger.isErrorEnabled).thenReturn(true)
        when(mockLogger.isInfoEnabled).thenReturn(true)
        val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
          Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
        whenReady(block(response).value) { actual =>
          actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))
          verifyCalls(info = Some(dummyContent))
        }
      }
    }
    warnLevel.foreach { httpResponseCode =>
      s"log message: WARNING level only when response is $httpResponseCode" in {
        reset(mockLogger)
        when(mockLogger.isErrorEnabled).thenReturn(true)
        when(mockLogger.isInfoEnabled).thenReturn(true)
        val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
          Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
        whenReady(block(response).value) { actual =>
          actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))
          verifyCalls(warn = Some(dummyContent))
        }
      }
    }
    logErrorWithThrowable(block, errorLevelWithThrowable)
    logErrorWithoutThrowable(block, errorLevelWithoutThrowable)
    logErrorRecoverBadGateway(block)
    logNothing(block)
  }

  private def logErrorWithThrowable(
      block: Future[Either[UpstreamErrorResponse, HttpResponse]] => EitherT[Future, UpstreamErrorResponse, HttpResponse],
      errorLevelWithThrowable: Set[Int]
    ): Unit =
    errorLevelWithThrowable.foreach { httpResponseCode =>
      s"log message: ERROR level only WITH throwable when response code is $httpResponseCode" in {
        reset(mockLogger)
        when(mockLogger.isErrorEnabled).thenReturn(true)
        when(mockLogger.isInfoEnabled).thenReturn(true)
        val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
          Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
        whenReady(block(response).value) { actual =>
          actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))
          verifyCalls(errorWithThrowable = Some(dummyContent))
        }
      }
    }

  private def logErrorWithoutThrowable(
      block: Future[Either[UpstreamErrorResponse, HttpResponse]] => EitherT[Future, UpstreamErrorResponse, HttpResponse],
      errorLevelWithoutThrowable: Set[Int]
    ): Unit =
    errorLevelWithoutThrowable.foreach { httpResponseCode =>
      s"log message: ERROR level only WITHOUT throwable when response code is $httpResponseCode" in {
        reset(mockLogger)
        when(mockLogger.isErrorEnabled).thenReturn(true)
        when(mockLogger.isInfoEnabled).thenReturn(true)
        val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
          Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
        whenReady(block(response).value) { actual =>
          actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))
          verifyCalls(errorWithoutThrowable = Some(dummyContent))
        }
      }
    }

  private def logErrorRecoverBadGateway(
      block: Future[Either[UpstreamErrorResponse, HttpResponse]] => EitherT[Future, UpstreamErrorResponse, HttpResponse]
    ): Unit =
    "log message: ERROR level only WITHOUT throwable when it failed with HttpException & recover to BAD GATEWAY" in {
      reset(mockLogger)
      when(mockLogger.isErrorEnabled).thenReturn(true)
      when(mockLogger.isInfoEnabled).thenReturn(true)
      val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
        Future.failed(new HttpException(dummyContent, GATEWAY_TIMEOUT))
      whenReady(block(response).value) { actual =>
        actual mustBe Left(UpstreamErrorResponse(dummyContent, BAD_GATEWAY))
        verifyCalls(errorWithoutThrowable = Some(dummyContent))
      }
    }

  private def logNothing(
      block: Future[Either[UpstreamErrorResponse, HttpResponse]] => EitherT[Future, UpstreamErrorResponse, HttpResponse]
    ): Unit =
    "log nothing at all when future failed with non-HTTPException" in {
      reset(mockLogger)
      when(mockLogger.isErrorEnabled).thenReturn(true)
      when(mockLogger.isInfoEnabled).thenReturn(true)
      val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
        Future.failed(new RuntimeException(dummyContent))

      recoverToSucceededIf[RuntimeException] {
        block(response).value
      }
      verifyCalls()
    }

  private def verifyCalls(
      info: Option[String]                  = None,
      warn: Option[String]                  = None,
      errorWithThrowable: Option[String]    = None,
      errorWithoutThrowable: Option[String] = None
    ): Unit = {

    val infoTimes                  = info.map(_ => 1).getOrElse(0)
    val warnTimes                  = warn.map(_ => 1).getOrElse(0)
    val errorWithThrowableTimes    = errorWithThrowable.map(_ => 1).getOrElse(0)
    val errorWithoutThrowableTimes = errorWithoutThrowable.map(_ => 1).getOrElse(0)

    def argumentMatcher(content: Option[String]): String = content match {
      case None    => ArgumentMatchers.any()
      case Some(c) => ArgumentMatchers.eq(c)
    }

    Mockito
      .verify(mockLogger, times(infoTimes))
      .info(argumentMatcher(info))
    Mockito
      .verify(mockLogger, times(warnTimes))
      .warn(argumentMatcher(warn))
    Mockito
      .verify(mockLogger, times(errorWithThrowableTimes))
      .error(argumentMatcher(errorWithThrowable), ArgumentMatchers.any())
    Mockito
      .verify(mockLogger, times(errorWithoutThrowableTimes))
      .error(argumentMatcher(errorWithoutThrowable))
  }

}
