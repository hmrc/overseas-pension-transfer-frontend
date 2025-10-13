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

import base.SpecBase
import config.FrontendAppConfig
import models.responses.InternalServerError
import models.{DashboardData, PensionSchemeDetails, PstrNumber, SrnNumber}
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import pages.DashboardPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.DashboardSessionRepository
import services.TransferService
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.PaginatedAllTransfersViewModel
import views.html.DashboardView

import scala.concurrent.Future

class DashboardControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  "DashboardController onPageLoad" - {

    "must return OK and render the view when dashboard data exists and transfers fetch succeeds" in {
      val mockRepo             = mock[DashboardSessionRepository]
      val mockService          = mock[TransferService]
      val pensionSchemeDetails =
        PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name")

      val dd = DashboardData(id = "user-1")
        .set(PensionSchemeDetailsQuery, pensionSchemeDetails).success.value

      when(mockRepo.get(any[String])).thenReturn(Future.successful(Some(dd)))
      when(mockRepo.set(any[DashboardData])).thenReturn(Future.successful(true))
      when(mockService.getAllTransfersData(meq(dd), meq(pensionSchemeDetails.pstrNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(dd)))
      when(mockRepo.findExpiringWithin7Days(any())).thenReturn(Seq.empty)

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[TransferService].toInstance(mockService)
          )
          .configure("ui.transfers.pageSize" -> 10)
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        val view      = application.injector.instanceOf[DashboardView]
        val appConfig = application.injector.instanceOf[FrontendAppConfig]

        val items = dd.get(TransfersOverviewQuery).getOrElse(Seq.empty)

        val vm = PaginatedAllTransfersViewModel.build(
          items      = items,
          page       = 1,
          pageSize   = appConfig.transfersPerPage,
          urlForPage = routes.DashboardController.onPageLoad(_).url
        )(stubMessages())

        val expectedHtml =
          view(
            schemeName    = pensionSchemeDetails.schemeName,
            nextPage      = DashboardPage.nextPage(dd).url,
            vm            = vm,
            expiringItems = Seq.empty
          )(request, messages(application)).toString

        status(result) mustBe OK
        contentAsString(result) mustBe expectedHtml

        verify(mockRepo).get(any[String])
        verify(mockRepo).set(any[DashboardData])
        verify(mockRepo).findExpiringWithin7Days(any())
        verify(mockService).getAllTransfersData(meq(dd), meq(pensionSchemeDetails.pstrNumber))(any[HeaderCarrier])
      }
    }

    "must redirect to Journey Recovery when no dashboard data exists" in {
      val mockRepo    = mock[DashboardSessionRepository]
      val mockService = mock[TransferService]

      when(mockRepo.get(any[String])).thenReturn(Future.successful(None))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[TransferService].toInstance(mockService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when PensionSchemeDetails are missing" in {
      val mockRepo    = mock[DashboardSessionRepository]
      val mockService = mock[TransferService]

      val dd = DashboardData(id = "user-2")

      when(mockRepo.get(any[String])).thenReturn(Future.successful(Some(dd)))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[TransferService].toInstance(mockService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery when transfer service fails" in {
      val mockRepo             = mock[DashboardSessionRepository]
      val mockService          = mock[TransferService]
      val pensionSchemeDetails =
        PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name")

      val dd = DashboardData(id = "user-3")
        .set(PensionSchemeDetailsQuery, pensionSchemeDetails).success.value

      when(mockRepo.get(any[String])).thenReturn(Future.successful(Some(dd)))
      when(mockService.getAllTransfersData(meq(dd), meq(pensionSchemeDetails.pstrNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(InternalServerError)))

      val application =
        applicationBuilder(userAnswers = emptyUserAnswers)
          .overrides(
            bind[DashboardSessionRepository].toInstance(mockRepo),
            bind[TransferService].toInstance(mockService)
          )
          .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
