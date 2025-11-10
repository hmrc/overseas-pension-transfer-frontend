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
import models._
import models.audit.JourneyStartedType.ContinueTransfer
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.{DashboardSessionRepository, SessionRepository}
import services.{AuditService, LockService, TransferService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.{Lock, LockRepository}
import views.html.DashboardView

import java.time.{Instant, LocalDate}
import scala.concurrent.Future
import scala.concurrent.duration.Duration

class DashboardControllerSpec extends AnyFreeSpec with SpecBase with MockitoSugar {

  implicit class FakeRequestOps[A](req: FakeRequest[A]) {

    def withQueryStringParameters(params: (String, String)*): FakeRequest[A] = {
      val queryString = params.map { case (k, v) => s"$k=${java.net.URLEncoder.encode(v, "UTF-8")}" }.mkString("&")
      val uri         = req.uri.split('?').headOption.getOrElse(req.uri)
      req.withTarget(req.target.withUriString(s"$uri?$queryString"))
    }
  }

  "DashboardController" - {

    "must render the dashboard data successfully" in {
      val mockRepo    = mock[DashboardSessionRepository]
      val mockService = mock[TransferService]
      val mockSession = mock[SessionRepository]
      val mockLock    = mock[LockRepository]
      val mockView    = mock[DashboardView]

      val pensionScheme = PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name")
      val transferItem  = AllTransfersItem(
        transferId      = userAnswersTransferNumber,
        qtVersion       = Some("v1"),
        qtStatus        = Some(QtStatus.InProgress),
        nino            = Some("AA123456A"),
        memberFirstName = Some("John"),
        memberSurname   = Some("Doe"),
        qtDate          = Some(LocalDate.now),
        lastUpdated     = Some(Instant.now),
        pstrNumber      = Some(PstrNumber("12345678AB")),
        submissionDate  = None
      )

      val dd = DashboardData("id")
        .set(PensionSchemeDetailsQuery, pensionScheme).success.value
        .set(TransfersOverviewQuery, Seq(transferItem)).success.value

      when(mockSession.clear(any())).thenReturn(Future.successful(true))
      when(mockRepo.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockRepo.set(any())).thenReturn(Future.successful(true))
      when(mockRepo.findExpiringWithin2Days(any())).thenReturn(Seq.empty)
      when(mockService.getAllTransfersData(meq(dd), meq(pensionScheme.pstrNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(dd)))
      when(mockView.apply(any(), any(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html("dashboard view"))

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[TransferService].toInstance(mockService),
          bind[SessionRepository].toInstance(mockSession),
          bind[LockRepository].toInstance(mockLock),
          bind[DashboardView].toInstance(mockView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad(1).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("dashboard view")

        verify(mockRepo).get(any())
        verify(mockService).getAllTransfersData(meq(dd), meq(pensionScheme.pstrNumber))(any[HeaderCarrier])
        verify(mockRepo).set(any())
      }
    }

    "must acquire lock when accessing an InProgress transfer (onTransferClick) and redirect" in {
      val mockRepo           = mock[DashboardSessionRepository]
      val mockService        = mock[TransferService]
      val mockSessionRepo    = mock[SessionRepository]
      val mockLockRepository = mock[LockRepository]

      when(mockLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(Some(mock[Lock])))

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[TransferService].toInstance(mockService),
          bind[SessionRepository].toInstance(mockSessionRepo),
          bind[LockRepository].toInstance(mockLockRepository)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.DashboardController.onTransferClick().url + "?transferId=QT123456&qtStatus=InProgress&name=SomeName&currentPage=1&pstr=PSTR123456"
        )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        verify(mockLockRepository, times(1)).takeLock(meq("QT123456"), any(), any())
      }
    }

    "must show warning when trying to access a locked record (takeLock returns None)" in {
      val mockRepo           = mock[DashboardSessionRepository]
      val mockService        = mock[TransferService]
      val mockSessionRepo    = mock[SessionRepository]
      val mockLockRepository = mock[LockRepository]

      when(mockLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(None)) // lock already taken

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[TransferService].toInstance(mockService),
          bind[SessionRepository].toInstance(mockSessionRepo),
          bind[LockRepository].toInstance(mockLockRepository)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.DashboardController.onTransferClick().url + "?transferId=QT123456&qtStatus=InProgress&memberName=LockedScheme&currentPage=2&pstr=PSTR123456"
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must include(routes.DashboardController.onPageLoad(2).url.split('?').head)

        flash(result).get("lockWarning") mustBe Some("LockedScheme")

        verify(mockLockRepository, times(1)).takeLock(meq("QT123456"), any(), any())
      }
    }

    "must releaseLock for items in TransfersOverviewQuery when dashboard loads" in {
      val mockRepo           = mock[DashboardSessionRepository]
      val mockService        = mock[TransferService]
      val mockSessionRepo    = mock[SessionRepository]
      val mockLockRepository = mock[LockRepository]
      val mockView           = mock[DashboardView]

      val pensionScheme = PensionSchemeDetails(SrnNumber("S111"), PstrNumber("PSTR111"), "SchemeX")

      // two transfers: one with transferReference, one with qtReference, one with neither
      val transfers = Seq(
        AllTransfersItem(
          transferId      = userAnswersTransferNumber,
          qtVersion       = None,
          qtStatus        = None,
          nino            = None,
          memberFirstName = None,
          memberSurname   = None,
          qtDate          = None,
          lastUpdated     = Some(Instant.now),
          pstrNumber      = Some(PstrNumber("PSTR111")),
          submissionDate  = None
        ),
        AllTransfersItem(
          transferId      = testQtNumber,
          qtVersion       = None,
          qtStatus        = None,
          nino            = None,
          memberFirstName = None,
          memberSurname   = None,
          qtDate          = None,
          lastUpdated     = Some(Instant.now),
          pstrNumber      = Some(PstrNumber("PSTR111")),
          submissionDate  = None
        ),
        AllTransfersItem(
          transferId      = QtNumber("QT987654"),
          qtVersion       = None,
          qtStatus        = None,
          nino            = None,
          memberFirstName = None,
          memberSurname   = None,
          qtDate          = None,
          lastUpdated     = Some(Instant.now),
          pstrNumber      = Some(PstrNumber("PSTR111")),
          submissionDate  = None
        )
      )

      val dd = DashboardData("id")
        .set(PensionSchemeDetailsQuery, pensionScheme).success.value
        .set(TransfersOverviewQuery, transfers).success.value

      when(mockSessionRepo.clear(any())).thenReturn(Future.successful(true))
      when(mockRepo.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockRepo.set(any())).thenReturn(Future.successful(true))
      when(mockService.getAllTransfersData(meq(dd), meq(pensionScheme.pstrNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(dd)))
      when(mockRepo.findExpiringWithin2Days(any())).thenReturn(Seq.empty)
      when(mockView.apply(any(), any(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html("dashboard"))

      when(mockLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[SessionRepository].toInstance(mockSessionRepo),
          bind[TransferService].toInstance(mockService),
          bind[LockRepository].toInstance(mockLockRepository),
          bind[DashboardView].toInstance(mockView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad(1).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("dashboard")

        // verify releaseLock called for the two items that had references
        verify(mockLockRepository, times(1)).releaseLock(meq(userAnswersTransferNumber.value), meq("A123456"))
        verify(mockLockRepository, times(1)).releaseLock(meq(testQtNumber.value), meq("A123456"))
        verify(mockLockRepository, times(1)).releaseLock(meq("QT987654"), meq("A123456"))
        verify(mockLockRepository, times(3)).releaseLock(any(), any())
      }
    }

    "must be able to acquire lock after a release (simulate unlock then access)" in {
      val mockRepo           = mock[DashboardSessionRepository]
      val mockService        = mock[TransferService]
      val mockSessionRepo    = mock[SessionRepository]
      val mockLockRepository = mock[LockRepository]

      when(mockLockRepository.takeLock(any[String], any[String], any[Duration])).thenReturn(Future.successful(Some(mock[Lock])))

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[SessionRepository].toInstance(mockSessionRepo),
          bind[TransferService].toInstance(mockService),
          bind[LockRepository].toInstance(mockLockRepository)
        )
        .build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.DashboardController.onTransferClick().url + "?transferId=QT654321&qtStatus=InProgress&name=ReAccess&currentPage=1&pstr=PSTR123456"
        )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        verify(mockLockRepository, times(1)).takeLock(meq("QT654321"), any(), any())
      }
    }

    "must display the 2 day expiry warning when repository returns expiring items" in {
      val mockRepo    = mock[DashboardSessionRepository]
      val mockService = mock[TransferService]
      val mockSession = mock[SessionRepository]
      val mockLock    = mock[LockRepository]
      val mockView    = mock[DashboardView]

      val pensionScheme = PensionSchemeDetails(SrnNumber("SEXP"), PstrNumber("PSTR-EXP"), "Expiring Scheme")

      val expiringTransfer = AllTransfersItem(
        transferId      = userAnswersTransferNumber,
        qtVersion       = None,
        qtStatus        = Some(QtStatus.InProgress),
        nino            = None,
        memberFirstName = Some("Alice"),
        memberSurname   = Some("Smith"),
        qtDate          = None,
        lastUpdated     = Some(Instant.now),
        pstrNumber      = Some(PstrNumber("PSTR-EXP")),
        submissionDate  = None
      )

      val dd = DashboardData("id")
        .set(PensionSchemeDetailsQuery, pensionScheme).success.value
        .set(TransfersOverviewQuery, Seq(expiringTransfer)).success.value

      when(mockSession.clear(any())).thenReturn(Future.successful(true))
      when(mockRepo.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockRepo.set(any())).thenReturn(Future.successful(true))
      when(mockService.getAllTransfersData(meq(dd), meq(pensionScheme.pstrNumber))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(dd)))

      when(mockRepo.findExpiringWithin2Days(any())).thenReturn(Seq(expiringTransfer))

      when(mockView.apply(any(), any(), any(), any())(any(), any())).thenReturn(play.twirl.api.Html("expiring soon banner"))

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[TransferService].toInstance(mockService),
          bind[SessionRepository].toInstance(mockSession),
          bind[LockRepository].toInstance(mockLock),
          bind[DashboardView].toInstance(mockView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad(1).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("expiring soon banner")
        verify(mockRepo, times(1)).findExpiringWithin2Days(any())
      }
    }
  }

  "onTransferClick with InProgress transfer" - {
    "must acquire lock, audit, and redirect when successful" in {

      val mockRepo           = mock[DashboardSessionRepository]
      val mockService        = mock[TransferService]
      val mockSessionRepo    = mock[SessionRepository]
      val mockLockService    = mock[LockService]
      val mockUserAnswersSvc = mock[UserAnswersService]
      val mockAuditService   = mock[AuditService]

      val transferId = TransferId("QT654321")
      val owner      = "A123456"

      val emptyUserAnswers = UserAnswers(
        id          = transferId,
        pstr        = PstrNumber("PSTR000"),
        lastUpdated = Instant.now(),
        data        = Json.obj()
      )

      when(mockUserAnswersSvc.getExternalUserAnswers(any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(emptyUserAnswers)))

      when(mockLockService.takeLockWithAudit(
        any(),
        any(),
        any(),
        any(),
        any(),
        any(),
        any()
      )(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder()
        .overrides(
          bind[DashboardSessionRepository].toInstance(mockRepo),
          bind[TransferService].toInstance(mockService),
          bind[SessionRepository].toInstance(mockSessionRepo),
          bind[LockService].toInstance(mockLockService),
          bind[UserAnswersService].toInstance(mockUserAnswersSvc),
          bind[AuditService].toInstance(mockAuditService)
        ).build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.DashboardController.onTransferClick().url +
            "?transferId=QT654321&qtStatus=InProgress&name=SchemeX&currentPage=1&pstr=PSTR123456"
        )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        verify(mockLockService, times(1)).takeLockWithAudit(
          meq(transferId),
          meq(owner),
          any(),
          any(),
          any(),
          meq(ContinueTransfer),
          any()
        )(any())
      }
    }
  }
}
