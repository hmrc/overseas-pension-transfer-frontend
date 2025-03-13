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
import forms.MemberSelectLastUkAddressFormProvider
import models.{AddressRecord, Country, NormalMode, RecordSet, UkAddress, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{MemberSelectLastUkAddressPage, MembersLastUkAddressLookupPage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.{JourneyRecoveryContinueView, MemberSelectLastUkAddressView}

import scala.concurrent.Future
import scala.util.Try

class MemberSelectLastUkAddressControllerSpec extends SpecBase with MockitoSugar {

  private lazy val memberSelectLastUkAddressRoute = routes.MemberSelectLastUkAddressController.onPageLoad(NormalMode).url

  private val addressRecords =
    RecordSet(addresses =
      Seq(
        AddressRecord(
          id      = "GB990091234514",
          address = UkAddress(
            lines       = List("2 Other Place", "Some District"),
            town        = "Anytown",
            rawPostCode = "ZZ1 1ZZ",
            rawCountry  = Country(code = "GB", name = "United Kingdom")
          )
        ),
        AddressRecord(
          id      = "GB990091234515",
          address = UkAddress(
            lines       = List("3 Other Place", "Some District"),
            town        = "Anytown",
            rawPostCode = "ZZ1 1ZZ",
            rawCountry  = Country(code = "GB", name = "United Kingdom")
          )
        )
      )
    )

  private val validIds = addressRecords.addresses.map(_.id)

  private val formProvider = new MemberSelectLastUkAddressFormProvider()
  private val form         = formProvider(validIds)

  val postcode: String = addressRecords.addresses.head.address.postcode.get

  val userAnswers: UserAnswers = UserAnswers("id").set(MembersLastUkAddressLookupPage, addressRecords).get

  "MemberSelectLastUkAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MemberSelectLastUkAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, addressRecords, postcode)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", validIds.head))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual MemberSelectLastUkAddressPage.nextPage(NormalMode, emptyUserAnswers).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))
        val view      = application.injector.instanceOf[MemberSelectLastUkAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, addressRecords, postcode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, memberSelectLastUkAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, memberSelectLastUkAddressRoute)
            .withFormUrlEncodedBody(("value", validIds.head))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
