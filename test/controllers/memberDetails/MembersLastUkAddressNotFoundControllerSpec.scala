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

package controllers.memberDetails

import base.{AddressBase, SpecBase}
import models.{CheckMode, NormalMode}
import org.scalatest.freespec.AnyFreeSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.memberDetails.MembersLastUkAddressNotFoundView

class MembersLastUkAddressNotFoundControllerSpec extends AnyFreeSpec with SpecBase with AddressBase {

  "MemberLastUkAddressNotFound Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = noAddressFoundUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.MembersLastUkAddressNotFoundController.onPageLoad(NormalMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersLastUkAddressNotFoundView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(noAddressFound.postcode, NormalMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }

    "must return OK and the correct view for a GET in CheckMode" in {

      val application = applicationBuilder(userAnswers = noAddressFoundUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.MembersLastUkAddressNotFoundController.onPageLoad(CheckMode).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersLastUkAddressNotFoundView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(noAddressFound.postcode, CheckMode)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}
