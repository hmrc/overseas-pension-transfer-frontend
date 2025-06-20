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

package controllers

import base.SpecBase
import models.NormalMode
import org.scalatest.freespec.AnyFreeSpec
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatWillBeNeededView

class WhatWillBeNeededControllerSpec extends AnyFreeSpec with SpecBase {

  "WhatWillBeNeeded Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, routes.WhatWillBeNeededController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatWillBeNeededView]

        // TODO this will change to TaskListController once implemented
        val nextPage = controllers.memberDetails.routes.MemberNameController.onPageLoad(mode = NormalMode).url

        status(result) mustEqual OK

        contentAsString(result) mustEqual view(nextPage)(request, messages(application)).toString
      }
    }
  }
}
