/*
 * Copyright 2024 HM Revenue & Customs
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

import controllers.actions._
import models.authentication.{PsaId, PsaUser, PspId, PspUser}
import models.requests.DisplayRequest
import models.{PersonName, QtNumber, UserAnswers}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import pages.memberDetails.MemberNamePage
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import queries.{DateSubmittedQuery, QtNumberQuery}

import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, FormatStyle}

trait SpecBase
    extends Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience {

  val testMemberName: PersonName = PersonName("User", "McUser")

  val testQtNumber: QtNumber = QtNumber("QT123456")

  val userAnswersId: String = "id"

  val psaId: PsaId = PsaId("A123456")

  val psaUser: PsaUser = PsaUser(psaId, internalId = userAnswersId)

  val pspId = PspId("X7654321")

  val pspUser: PspUser = PspUser(pspId, internalId = userAnswersId)

  val testDateTransferSubmitted: LocalDateTime   = LocalDateTime.now
  val formattedTestDateTransferSubmitted: String = testDateTransferSubmitted.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT))

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def userAnswersMemberName: UserAnswers = emptyUserAnswers.set(MemberNamePage, testMemberName).success.value

  def userAnswersQtNumber: UserAnswers = emptyUserAnswers.set(QtNumberQuery, testQtNumber).success.value

  def userAnswersMemberNameQtNumber: UserAnswers = userAnswersMemberName.set(QtNumberQuery, testQtNumber).success.value

  def userAnswersMemberNameQtNumberTransferSubmitted: UserAnswers =
    userAnswersMemberNameQtNumber.set(DateSubmittedQuery, testDateTransferSubmitted).success.value

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[MarkInProgressOnEntryAction].to[FakeMarkInProgressAction],
        bind[DisplayAction].to[FakeDisplayAction]
      )

  def fakeDisplayRequest[A](fakeRequest: FakeRequest[A], userAnswers: UserAnswers = emptyUserAnswers): DisplayRequest[A] =
    DisplayRequest(
      request               = fakeRequest,
      authenticatedUser     = psaUser,
      userAnswers           = userAnswers,
      memberName            = testMemberName.fullName,
      qtNumber              = testQtNumber,
      dateTransferSubmitted = formattedTestDateTransferSubmitted
    )

  implicit val testDisplayRequest: DisplayRequest[_] =
    DisplayRequest(
      request               = FakeRequest(),
      authenticatedUser     = psaUser,
      userAnswers           = emptyUserAnswers,
      memberName            = testMemberName.fullName,
      qtNumber              = testQtNumber,
      dateTransferSubmitted = formattedTestDateTransferSubmitted
    )

}
