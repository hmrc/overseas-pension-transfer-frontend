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

package controllers.viewandamend

import base.SpecBase
import models.responses.UserAnswersErrorResponse
import models.{AmendCheckMode, FinalCheckMode, PstrNumber, QtStatus, TransferId, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{LockService, UserAnswersService}
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.checkAnswers.memberDetails.MemberDetailsSummary
import viewmodels.checkAnswers.qropsDetails.QROPSDetailsSummary
import viewmodels.checkAnswers.qropsSchemeManagerDetails.SchemeManagerDetailsSummary
import viewmodels.checkAnswers.schemeOverview.SchemeDetailsSummary
import viewmodels.checkAnswers.transferDetails.TransferDetailsSummary
import viewmodels.govuk.SummaryListFluency
import views.html.viewandamend.ViewSubmittedView

import scala.concurrent.Future

class ViewAmendSubmittedControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with MockitoSugar
    with SummaryListFluency {

  private val mockUserAnswersService = mock[UserAnswersService]
  private val mockLockService        = mock[LockService]
  private val mockSessionRepository  = mock[SessionRepository]
  private val qtStatus               = QtStatus.Submitted
  private val versionNumber          = "007"

  private def schemeSummaryList =
    SummaryListViewModel(
      SchemeDetailsSummary.rows(
        FinalCheckMode,
        schemeDetails.schemeName,
        formattedTestDateTransferSubmitted
      )(messages(applicationBuilder().build()))
    )

  private def memberDetailsSummaryList =
    SummaryListViewModel(
      MemberDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(applicationBuilder().build()))
    )

  private def transferDetailsSummaryList =
    SummaryListViewModel(
      TransferDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(applicationBuilder().build()))
    )

  private def qropsDetailsSummaryList =
    SummaryListViewModel(
      QROPSDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(applicationBuilder().build()))
    )

  private def schemeManagerDetailsSummaryList =
    SummaryListViewModel(
      SchemeManagerDetailsSummary.rows(
        FinalCheckMode,
        userAnswersMemberNameQtNumberTransferSubmitted,
        showChangeLinks = false
      )(messages(applicationBuilder().build()))
    )

  "ViewAmendSubmittedController" - {

    "view" - {

      "return Ok and render expected view (uses sessionData.transferId and memberName)" in {
        when(
          mockUserAnswersService.getExternalUserAnswers(
            any[TransferId],
            any[PstrNumber],
            any[QtStatus],
            any[Option[String]]
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(Right(userAnswersMemberNameQtNumber)))

        val app =
          applicationBuilder(
            userAnswers = userAnswersMemberNameQtNumber,
            sessionData = sessionDataMemberNameQtNumber
          ).overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService)
          ).build()

        val req    = FakeRequest(GET, routes.ViewAmendSubmittedController.view(testQtNumber, pstr, qtStatus, versionNumber).url)
        val result = route(app, req).value

        val view = app.injector.instanceOf[ViewSubmittedView]

        status(result) mustBe OK
        contentAsString(result) mustBe view(
          AmendCheckMode,
          schemeSummaryList,
          memberDetailsSummaryList,
          transferDetailsSummaryList,
          qropsDetailsSummaryList,
          schemeManagerDetailsSummaryList,
          testQtNumber.value,
          testMemberName.fullName,
          isAmend   = false,
          isChanged = false
        )(
          fakeIdentifierRequest(
            req
          ),
          messages(app)
        ).toString

        app.stop()
      }

      "redirect to JourneyRecovery when external answers lookup fails" in {
        when(
          mockUserAnswersService.getExternalUserAnswers(
            any[TransferId],
            any[PstrNumber],
            any[QtStatus],
            any[Option[String]]
          )(any[HeaderCarrier])
        ).thenReturn(Future.successful(Left(UserAnswersErrorResponse("boom", None))))

        val app = applicationBuilder()
          .overrides(bind[UserAnswersService].toInstance(mockUserAnswersService))
          .build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.view(testQtNumber, pstr, qtStatus, versionNumber).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        app.stop()
      }
    }

    "fromDraft" - {
      "redirect to amend page when user answers are fetched and lock acquired" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(userAnswersMemberNameQtNumber)))
        when(mockUserAnswersService.toAllTransfersItem(any())).thenReturn(transferItem)
        when(mockLockService.takeLockWithAudit(any(), any(), any(), any(), any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder()
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[LockService].toInstance(mockLockService),
            bind[SessionRepository].toInstance(mockSessionRepository)
          ).build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.fromDraft(testQtNumber, pstr, qtStatus, versionNumber).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ViewAmendSubmittedController.amend().url

        app.stop()
      }

      "redirect back to submitted summary when lock not acquired" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(userAnswersMemberNameQtNumber)))
        when(mockUserAnswersService.toAllTransfersItem(any())).thenReturn(transferItem)
        when(mockLockService.takeLockWithAudit(any(), any(), any(), any(), any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(false))

        val app = applicationBuilder()
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[LockService].toInstance(mockLockService)
          ).build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.fromDraft(testQtNumber, pstr, qtStatus, versionNumber).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SubmittedTransferSummaryController
          .onPageLoad(testQtNumber, pstr, qtStatus, versionNumber)
          .url
        flash(result).get("lockWarning") mustBe defined

        app.stop()
      }

      "redirect to JourneyRecovery when user answers retrieval fails" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Left(UserAnswersErrorResponse("boom", None))))
        when(mockLockService.takeLockWithAudit(any(), any(), any(), any(), any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(true))

        val app = applicationBuilder()
          .overrides(
            bind[UserAnswersService].toInstance(mockUserAnswersService),
            bind[LockService].toInstance(mockLockService)
          ).build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.fromDraft(testQtNumber, pstr, qtStatus, versionNumber).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.JourneyRecoveryController.onPageLoad().url

        app.stop()
      }
    }

    "amend" - {

      "return Ok with isChanged = true when local and external user answers differ" in {
        val localUserAnswers    = userAnswersMemberNameQtNumber
        val externalUserAnswers = localUserAnswers.copy(data = localUserAnswers.data ++ play.api.libs.json.Json.obj("newField" -> "newValue"))

        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(externalUserAnswers)))
        when(mockUserAnswersService.toAllTransfersItem(any())).thenReturn(transferItem)
        when(mockLockService.takeLockWithAudit(any(), any(), any(), any(), any(), any(), any())(any[HeaderCarrier]))
          .thenReturn(Future.successful(true))
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val app = applicationBuilder(
          userAnswers = localUserAnswers,
          sessionData = sessionDataMemberNameQtNumber
        ).overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        ).build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.amend().url)
        val result  = route(app, request).value

        status(result) mustBe OK
        contentAsString(result).toLowerCase must include("continue")

        app.stop()
      }

      "return Ok with isChanged = false when local and external user answers are same" in {
        val localUserAnswers = userAnswersMemberNameQtNumber

        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Right(localUserAnswers)))

        val app = applicationBuilder(
          userAnswers = localUserAnswers,
          sessionData = sessionDataMemberNameQtNumber
        ).overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        ).build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.amend().url)
        val result  = route(app, request).value

        status(result) mustBe OK
        contentAsString(result) must not include ("continue")

        app.stop()
      }

      "return InternalServerError when external answers lookup fails" in {
        when(mockUserAnswersService.getExternalUserAnswers(any(), any(), any(), any())(any()))
          .thenReturn(Future.successful(Left(UserAnswersErrorResponse("failure", None))))

        val app = applicationBuilder(
          userAnswers = userAnswersMemberNameQtNumber,
          sessionData = sessionDataMemberNameQtNumber
        ).overrides(
          bind[UserAnswersService].toInstance(mockUserAnswersService)
        ).build()

        val request = FakeRequest(GET, routes.ViewAmendSubmittedController.amend().url)
        val result  = route(app, request).value

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) must include("Unable to fetch external user answers")

        app.stop()
      }
    }
  }
}
