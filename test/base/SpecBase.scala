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

import controllers.actions.*
import data.TestData
import models.{PensionSchemeDetails, SessionData, UserAnswers}
import models.address.{Countries, PropertyAddress}
import models.assets.TypeOfAsset
import models.authentication.*
import models.requests.{DisplayRequest, IdentifierRequest, SchemeRequest}
import org.mockito.Mockito.reset
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, Inside, OptionValues, TryValues}
import pages.transferDetails.assetsMiniJourneys.otherAssets.{OtherAssetsDescriptionPage, OtherAssetsValuePage}
import pages.transferDetails.assetsMiniJourneys.property.{PropertyAddressPage, PropertyDescriptionPage, PropertyValuePage}
import pages.transferDetails.assetsMiniJourneys.quotedShares.{QuotedSharesClassPage, QuotedSharesCompanyNamePage, QuotedSharesNumberPage, QuotedSharesValuePage}
import pages.transferDetails.assetsMiniJourneys.unquotedShares.{UnquotedSharesClassPage, UnquotedSharesCompanyNamePage, UnquotedSharesNumberPage, UnquotedSharesValuePage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.{Binding, bind}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import queries.QtNumberQuery
import repositories.{DashboardSessionRepository, ExpiringMongoLockRepository, SessionRepository}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.mongo.play.PlayMongoModule

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Awaitable}

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with MockitoSugar
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach
    with Inside
    with TestData {
  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  protected val mockDashboardSessionRepository: DashboardSessionRepository = mock[DashboardSessionRepository]
  protected val mockSessionRepository: SessionRepository                   = mock[SessionRepository]
  protected val mockMongoLockRepository: ExpiringMongoLockRepository       = mock[ExpiringMongoLockRepository]

  override protected def beforeEach(): Unit =
    reset(mockSessionRepository)

  protected def applicationBuilder(
    userAnswers: UserAnswers = emptyUserAnswers,
    sessionData: SessionData = sessionDataMemberNameQtNumber,
    identifierAction: Option[IdentifierAction] = None
  ): GuiceApplicationBuilder = {

    val identifierActionBinding: Binding[IdentifierAction] = identifierAction match {
      case None     => bind[IdentifierAction].to[FakeIdentifierAction]
      case Some(ia) => bind[IdentifierAction].toInstance(ia)
    }

    val bindings = Seq(
      bind[ExpiringMongoLockRepository].toInstance(mockMongoLockRepository),
      bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers, sessionData)),
      bind[CheckLockAction].toInstance(new FakeCheckLockAction),
      bind[SchemeDataAction].to[FakeSchemeDataAction],
      bind[DashboardSessionRepository].to(mockDashboardSessionRepository),
      bind[SessionRepository].to(mockSessionRepository)
    ) :+ identifierActionBinding

    new GuiceApplicationBuilder().disable[PlayMongoModule].overrides(bindings)
  }

  def fakeIdentifierRequest[A](
    fakeRequest: FakeRequest[A],
    authenticatedUser: AuthenticatedUser = psaUser
  ): IdentifierRequest[A] =
    IdentifierRequest(fakeRequest, authenticatedUser)

  implicit val testIdentifierRequest: IdentifierRequest[_] =
    IdentifierRequest(FakeRequest(), psaUser)

  def fakeSchemeRequest[A](
    fakeRequest: FakeRequest[A],
    authenticatedUser: AuthenticatedUser = psaUser,
    schemeDetails: PensionSchemeDetails = schemeDetails
  ): SchemeRequest[A] =
    SchemeRequest(fakeRequest, authenticatedUser, schemeDetails)

  def fakeDisplayRequest[A](
    fakeRequest: FakeRequest[A],
    userAnswers: UserAnswers = emptyUserAnswers,
    sessionData: SessionData = emptySessionData
  ): DisplayRequest[A] =
    DisplayRequest(
      request = fakeRequest,
      authenticatedUser = psaUser,
      userAnswers = userAnswers,
      sessionData = sessionData,
      memberName = testMemberName.fullName,
      qtNumber = testQtNumber,
      dateTransferSubmitted = formattedTestDateTransferSubmitted
    )

  implicit val testDisplayRequest: DisplayRequest[_] =
    DisplayRequest(
      request = FakeRequest(),
      authenticatedUser = psaUser,
      userAnswers = emptyUserAnswers,
      sessionData = emptySessionData,
      memberName = testMemberName.fullName,
      qtNumber = testQtNumber,
      dateTransferSubmitted = formattedTestDateTransferSubmitted
    )

  def userAnswersWithAssets(assetsCount: Int = 1): UserAnswers =
    (0 until assetsCount).foldLeft(
      emptyUserAnswers
        .set(QtNumberQuery, testQtNumber)
        .success
        .value
    ) { (ua, idx) =>
      val updatedUa =
        ua.set(
          PropertyAddressPage(idx),
          PropertyAddress(
            addressLine1 = s"${idx + 1} Property${idx + 1}",
            addressLine2 = s"Test address line ${idx + 1}",
            None,
            None,
            None,
            Countries.UK,
            None
          )
        ).success
          .value
          .set(PropertyValuePage(idx), BigDecimal(10000 + idx * 1000)) // incremental property values
          .success
          .value
          .set(PropertyDescriptionPage(idx), s"Description-${idx + 1}")
          .success
          .value

      // Add other assets
      val withOtherAssets =
        updatedUa
          .set(
            OtherAssetsDescriptionPage(idx),
            s"OtherAsset-${idx + 1}"
          )
          .success
          .value
          .set(OtherAssetsValuePage(idx), BigDecimal(200 + idx * 50))
          .success
          .value

      // Add unquoted shares
      val withUnquotedShares =
        withOtherAssets
          .set(
            UnquotedSharesCompanyNamePage(idx),
            s"UnquotedCompany-${idx + 1}"
          )
          .success
          .value
          .set(UnquotedSharesValuePage(idx), BigDecimal(300 + idx * 75))
          .success
          .value
          .set(UnquotedSharesNumberPage(idx), 400 + idx * 100)
          .success
          .value
          .set(UnquotedSharesClassPage(idx), "A")
          .success
          .value

      // Add quoted shares
      val withQuotedShares =
        withUnquotedShares
          .set(
            QuotedSharesCompanyNamePage(idx),
            s"QuotedCompany-${idx + 1}"
          )
          .success
          .value
          .set(QuotedSharesValuePage(idx), BigDecimal(500 + idx * 125))
          .success
          .value
          .set(QuotedSharesNumberPage(idx), 600 + idx * 150)
          .success
          .value
          .set(QuotedSharesClassPage(idx), "B")
          .success
          .value

      withQuotedShares
    }

  def completeJson(assetType: TypeOfAsset): JsObject =
    assetType match {

      case TypeOfAsset.Other =>
        Json.obj(
          "transferDetails" -> Json.obj(
            "otherAssets" -> Json.arr(
              Json.obj(
                "assetValue"       -> 1000,
                "assetDescription" -> "Gold"
              )
            )
          )
        )

      case TypeOfAsset.Property =>
        Json.obj(
          "transferDetails" -> Json.obj(
            "propertyAssets" -> Json.arr(
              Json.obj(
                "propertyAddress" -> "Line 1",
                "propValue"       -> 250000,
                "propDescription" -> "Residential"
              )
            )
          )
        )

      case TypeOfAsset.QuotedShares =>
        Json.obj(
          "transferDetails" -> Json.obj(
            "quotedShares" -> Json.arr(
              Json.obj(
                "quotedValue"      -> 200,
                "quotedShareTotal" -> 10,
                "quotedCompany"    -> "ABC PLC",
                "quotedClass"      -> "A"
              )
            )
          )
        )

      case TypeOfAsset.UnquotedShares =>
        Json.obj(
          "transferDetails" -> Json.obj(
            "unquotedShares" -> Json.arr(
              Json.obj(
                "unquotedValue"      -> 200,
                "unquotedShareTotal" -> 10,
                "unquotedCompany"    -> "ABC Ltd",
                "unquotedClass"      -> "A"
              )
            )
          )
        )

      case _ => Json.obj()
    }

  def incompleteJson(): JsObject =
    Json.obj("transferDetails" -> Json.obj())

  def await[T](awaitable: Awaitable[T]): T = Await.result(awaitable, Duration.Inf)

}
