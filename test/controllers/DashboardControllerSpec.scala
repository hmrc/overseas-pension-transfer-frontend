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
import models.*
import models.audit.JourneyStartedType.ContinueTransfer
import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import services.{AuditService, LockService, TransferService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.Lock
import views.html.DashboardView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class DashboardControllerSpec extends SpecBase {

  implicit class FakeRequestOps[A](req: FakeRequest[A]) {

    def withQueryStringParameters(params: (String, String)*): FakeRequest[A] = {
      val queryString = params.map { case (k, v) => s"$k=${java.net.URLEncoder.encode(v, "UTF-8")}" }.mkString("&")
      val uri         = req.uri.split('?').headOption.getOrElse(req.uri)
      req.withTarget(req.target.withUriString(s"$uri?$queryString"))
    }
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockDashboardSessionRepository)
    reset(mockMongoLockRepository)
  }

  "DashboardController" - {

    "must render the dashboard data successfully" in {
      val mockService     = mock[TransferService]
      val mockView        = mock[DashboardView]
      val mockLockService = mock[LockService]

      val transferItem = AllTransfersItem(
        transferId = testTransferNumber,
        qtVersion = Some("v1"),
        qtStatus = Some(QtStatus.InProgress),
        nino = Some("AA123456A"),
        memberFirstName = Some("John"),
        memberSurname = Some("Doe"),
        qtDate = Some(today),
        lastUpdated = Some(now),
        pstrNumber = Some(PstrNumber("12345678AB")),
        submissionDate = None
      )

      val dd = DashboardData
        .create("id", now)
        .set(PensionSchemeDetailsQuery, schemeDetails)
        .success
        .value
        .set(TransfersOverviewQuery, Seq(transferItem))
        .success
        .value

      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockDashboardSessionRepository.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockDashboardSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(mockDashboardSessionRepository.findExpiringWithin2Days(any())).thenReturn(Seq.empty)
      when(
        mockService.getAllTransfersData(eqTo(dd), eqTo(schemeDetails.pstrNumber), eqTo(schemeDetails.srnNumber))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(Right(dd)))
      when(mockView.apply(any(), any(), any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(play.twirl.api.Html("dashboard view"))

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService),
          bind[DashboardView].toInstance(mockView),
          bind[ExecutionContext].toInstance(global),
          bind[LockService].toInstance(mockLockService)
        )
        .build()

      when(mockLockService.releaseLock(any(), any())).thenReturn(Future.successful((): Unit))

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("dashboard view")

        verify(mockDashboardSessionRepository).get(any())
        verify(mockService)
          .getAllTransfersData(eqTo(dd), eqTo(schemeDetails.pstrNumber), eqTo(schemeDetails.srnNumber))(
            any[HeaderCarrier]
          )
        verify(mockDashboardSessionRepository).set(any())
      }
    }

    "must acquire lock when accessing an InProgress transfer (onTransferClick) and redirect" in {

      val mockService = mock[TransferService]

      when(mockMongoLockRepository.takeLock(any(), any())).thenReturn(Future.successful(Some(mock[Lock])))

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.DashboardController
            .onTransferClick()
            .url + "?transferId=QT123456&qtStatus=InProgress&name=SomeName&currentPage=1&pstr=PSTR123456"
        )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        verify(mockMongoLockRepository, times(1)).takeLock(eqTo("QT123456"), any())
      }
    }

    "must show warning when trying to access a locked record (takeLock returns None)" in {

      val mockService = mock[TransferService]

      when(mockMongoLockRepository.takeLock(any(), any()))
        .thenReturn(Future.successful(None)) // lock already taken

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request =
          FakeRequest(
            GET,
            routes.DashboardController
              .onTransferClick()
              .url + "?transferId=QT123456&qtStatus=InProgress&memberName=LockedScheme&currentPage=2&pstr=PSTR123456"
          )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value must include(routes.DashboardController.onPageLoad(2).url.split('?').head)

        flash(result).get("lockWarning") mustBe Some("LockedScheme")

        verify(mockMongoLockRepository, times(1)).takeLock(eqTo("QT123456"), any())
      }
    }

    "must releaseLock for items in TransfersOverviewQuery when dashboard loads" in {
      val mockService = mock[TransferService]

      val mockView = mock[DashboardView]

      // two transfers: one with transferReference, one with qtReference, one with neither
      val transfers = Seq(
        AllTransfersItem(
          transferId = testTransferNumber,
          qtVersion = None,
          qtStatus = None,
          nino = None,
          memberFirstName = None,
          memberSurname = None,
          qtDate = None,
          lastUpdated = Some(now),
          pstrNumber = Some(pstr),
          submissionDate = None
        ),
        AllTransfersItem(
          transferId = testQtNumber,
          qtVersion = None,
          qtStatus = None,
          nino = None,
          memberFirstName = None,
          memberSurname = None,
          qtDate = None,
          lastUpdated = Some(now),
          pstrNumber = Some(pstr),
          submissionDate = None
        ),
        AllTransfersItem(
          transferId = QtNumber("QT987654"),
          qtVersion = None,
          qtStatus = None,
          nino = None,
          memberFirstName = None,
          memberSurname = None,
          qtDate = None,
          lastUpdated = Some(now),
          pstrNumber = Some(pstr),
          submissionDate = None
        )
      )

      val dd = DashboardData
        .create("id", now)
        .set(PensionSchemeDetailsQuery, schemeDetails)
        .success
        .value
        .set(TransfersOverviewQuery, transfers)
        .success
        .value

      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockDashboardSessionRepository.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockDashboardSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(
        mockService.getAllTransfersData(eqTo(dd), eqTo(pstr), eqTo(srn))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(Right(dd)))
      when(mockDashboardSessionRepository.findExpiringWithin2Days(any())).thenReturn(Seq.empty)
      when(mockView.apply(any(), any(), any(), any(), any(), any(), any())(any(), any()))
        .thenReturn(play.twirl.api.Html("dashboard"))

      when(mockMongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService),
          bind[DashboardView].toInstance(mockView)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad().url)
        val result  = route(application, request).value

        status(result) mustBe OK
        contentAsString(result) must include("dashboard")

        // verify releaseLock called for the two items that had references
        verify(mockMongoLockRepository, times(1)).releaseLock(eqTo(testTransferNumber.value), eqTo(psaId.value))
        verify(mockMongoLockRepository, times(1)).releaseLock(eqTo(testQtNumber.value), eqTo(psaId.value))
        verify(mockMongoLockRepository, times(1)).releaseLock(eqTo("QT987654"), eqTo(psaId.value))
        verify(mockMongoLockRepository, times(3)).releaseLock(any(), any())
      }
    }

    "must be able to acquire lock after a release (simulate unlock then access)" in {

      val mockService = mock[TransferService]

      when(mockMongoLockRepository.takeLock(any(), any()))
        .thenReturn(Future.successful(Some(mock[Lock])))

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService)
        )
        .build()

      running(application) {

        val request = FakeRequest(
          GET,
          routes.DashboardController
            .onTransferClick()
            .url + "?transferId=QT654321&qtStatus=InProgress&name=ReAccess&currentPage=1&pstr=PSTR123456"
        )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER
        verify(mockMongoLockRepository, times(1)).takeLock(eqTo("QT654321"), any())
      }
    }

    "must render the search bar when dashboard search feature is enabled" in {
      when(mockMongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))
      val mockService = mock[TransferService]

      val pensionScheme = PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name")
      val transferItem  = AllTransfersItem(
        transferId = testTransferNumber,
        qtVersion = Some("v1"),
        qtStatus = Some(QtStatus.InProgress),
        nino = Some("AA123456A"),
        memberFirstName = Some("John"),
        memberSurname = Some("Doe"),
        qtDate = Some(today),
        lastUpdated = Some(now),
        pstrNumber = Some(PstrNumber("12345678AB")),
        submissionDate = None
      )

      val dd = DashboardData
        .create("id", now)
        .set(PensionSchemeDetailsQuery, pensionScheme)
        .success
        .value
        .set(TransfersOverviewQuery, Seq(transferItem))
        .success
        .value

      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockDashboardSessionRepository.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockDashboardSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(
        mockService.getAllTransfersData(eqTo(dd), eqTo(pensionScheme.pstrNumber), eqTo(pensionScheme.srnNumber))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(Right(dd)))
      when(mockDashboardSessionRepository.findExpiringWithin2Days(any())).thenReturn(Seq.empty)

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad(1, None).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("""id="dashboard-search"""")
        body must include("""name="search"""")
      }
    }

    "must filter transfers when a search term is provided and render the clear link" in {
      when(mockMongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))
      val mockService = mock[TransferService]

      val pensionScheme = PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name")

      val johnTransfer = AllTransfersItem(
        transferId = testTransferNumber,
        qtVersion = Some("v1"),
        qtStatus = Some(QtStatus.InProgress),
        nino = Some("AA123456A"),
        memberFirstName = Some("John"),
        memberSurname = Some("Doe"),
        qtDate = Some(today),
        lastUpdated = Some(now),
        pstrNumber = Some(PstrNumber("12345678AB")),
        submissionDate = None
      )

      val aliceTransfer = AllTransfersItem(
        transferId = testQtNumber,
        qtVersion = Some("v1"),
        qtStatus = Some(QtStatus.InProgress),
        nino = Some("BB123456B"),
        memberFirstName = Some("Alice"),
        memberSurname = Some("Smith"),
        qtDate = Some(today),
        lastUpdated = Some(now),
        pstrNumber = Some(PstrNumber("12345678AB")),
        submissionDate = None
      )

      val dd = DashboardData
        .create("id", now)
        .set(PensionSchemeDetailsQuery, pensionScheme)
        .success
        .value
        .set(TransfersOverviewQuery, Seq(johnTransfer, aliceTransfer))
        .success
        .value

      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockDashboardSessionRepository.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockDashboardSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(
        mockService.getAllTransfersData(eqTo(dd), eqTo(pensionScheme.pstrNumber), eqTo(pensionScheme.srnNumber))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(Right(dd)))
      when(mockDashboardSessionRepository.findExpiringWithin2Days(any())).thenReturn(Seq.empty)

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad(1, Some("John")).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val body = contentAsString(result)
        val doc  = Jsoup.parse(body)

        body must include("John")
        body must include("Doe")
        body must not include "Alice"
        body must not include "Smith"

        body must include("""id="dashboard-search"""")
        body must include("""name="search"""")
        body must include("""value="John"""")

        val clearLink = doc.select("a.search-bar__clear").first()
        clearLink        must not be null
        clearLink.text() must include(messages(application)("dashboard.search.clear"))

        val hiddenSpan = clearLink.select("span.govuk-visually-hidden").first()
        hiddenSpan must not be null
        hiddenSpan.text().trim mustBe messages(application)("dashboard.search.clear.hiddenText")
      }
    }

    "must show all transfers again when search term is cleared" in {
      when(mockMongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))
      val mockService = mock[TransferService]

      val pensionScheme = PensionSchemeDetails(SrnNumber("S1234567"), PstrNumber("12345678AB"), "Scheme Name")

      val johnTransfer = AllTransfersItem(
        transferId = testTransferNumber,
        qtVersion = Some("v1"),
        qtStatus = Some(QtStatus.InProgress),
        nino = Some("AA123456A"),
        memberFirstName = Some("John"),
        memberSurname = Some("Doe"),
        qtDate = Some(today),
        lastUpdated = Some(now),
        pstrNumber = Some(PstrNumber("12345678AB")),
        submissionDate = None
      )

      val aliceTransfer = AllTransfersItem(
        transferId = testQtNumber,
        qtVersion = Some("v1"),
        qtStatus = Some(QtStatus.InProgress),
        nino = Some("BB123456B"),
        memberFirstName = Some("Alice"),
        memberSurname = Some("Smith"),
        qtDate = Some(today),
        lastUpdated = Some(now),
        pstrNumber = Some(PstrNumber("12345678AB")),
        submissionDate = None
      )

      val dd = DashboardData
        .create("id", now)
        .set(PensionSchemeDetailsQuery, pensionScheme)
        .success
        .value
        .set(TransfersOverviewQuery, Seq(johnTransfer, aliceTransfer))
        .success
        .value

      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))
      when(mockDashboardSessionRepository.get(any())).thenReturn(Future.successful(Some(dd)))
      when(mockDashboardSessionRepository.set(any())).thenReturn(Future.successful(true))
      when(
        mockService.getAllTransfersData(eqTo(dd), eqTo(pensionScheme.pstrNumber), eqTo(pensionScheme.srnNumber))(
          any[HeaderCarrier]
        )
      )
        .thenReturn(Future.successful(Right(dd)))
      when(mockDashboardSessionRepository.findExpiringWithin2Days(any())).thenReturn(Seq.empty)

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService)
        )
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.onPageLoad(1, None).url)
        val result  = route(application, request).value

        status(result) mustBe OK
        val body = contentAsString(result)

        body must include("John")
        body must include("Doe")
        body must include("Alice")
        body must include("Smith")

        body must not include "search-bar__clear"
      }
    }
  }

  "onTransferClick with InProgress transfer" - {
    "must acquire lock, audit, and redirect when successful" in {

      val mockService = mock[TransferService]

      val mockLockService    = mock[LockService]
      val mockUserAnswersSvc = mock[UserAnswersService]
      val mockAuditService   = mock[AuditService]

      when(mockUserAnswersSvc.getExternalUserAnswers(any(), any(), any(), any(), any())(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(emptyUserAnswers)))

      when(
        mockLockService.takeLockWithAudit(
          any(),
          any(),
          any(),
          any(),
          any(),
          any()
        )(any())
      ).thenReturn(Future.successful(true))

      val application = applicationBuilder()
        .overrides(
          bind[TransferService].toInstance(mockService),
          bind[LockService].toInstance(mockLockService),
          bind[UserAnswersService].toInstance(mockUserAnswersSvc),
          bind[AuditService].toInstance(mockAuditService)
        )
        .build()

      running(application) {
        val request = FakeRequest(
          GET,
          routes.DashboardController.onTransferClick().url +
            s"?transferId=${testTransferNumber.value}&qtStatus=InProgress&name=$schemeName&currentPage=1&pstr=${pstr.value}"
        )

        val result = route(application, request).value

        status(result) mustBe SEE_OTHER

        verify(mockLockService, times(1)).takeLockWithAudit(
          eqTo(testTransferNumber),
          eqTo(psaId.value),
          any(),
          any(),
          eqTo(ContinueTransfer),
          any()
        )(any())
      }
    }
  }

  "clearAndExit" - {
    "must clear repositories and redirect to specified URL" in {

      when(mockDashboardSessionRepository.clear(any())).thenReturn(Future.successful(true))
      when(mockSessionRepository.clear(any())).thenReturn(Future.successful(true))

      val application = applicationBuilder()
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DashboardController.clearAndExit("/some-url").url)
        val result  = route(application, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe "/some-url"

        verify(mockDashboardSessionRepository, times(1)).clear(eqTo(testInternalId))
        verify(mockSessionRepository, times(1)).clear(eqTo(testInternalId))
      }
    }
  }
}
