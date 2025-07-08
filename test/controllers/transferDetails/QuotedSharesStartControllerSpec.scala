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

package controllers.transferDetails

import base.SpecBase
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.transferDetails.QuotedSharesStartView

class QuotedSharesStartControllerSpec extends AnyFreeSpec with SpecBase {

  "QuotedShareStart Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(userAnswersQtNumber)).build()

      running(application) {
        val request  = FakeRequest(GET, routes.QuotedSharesStartController.onPageLoad().url)
        val result   = route(application, request).value
        val view     = application.injector.instanceOf[QuotedSharesStartView]
        val nextPage = routes.QuotedSharesCompanyNameController.onPageLoad(NormalMode, 0).url

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(nextPage)(fakeDisplayRequest(request), messages(application)).toString
      }
    }
  }
}
