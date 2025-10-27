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

import base.SpecBase
import models.TaskCategory.{MemberDetails, QROPSDetails, SchemeManagerDetails, SubmissionDetails, TransferDetails}
import models.responses.UserAnswersErrorResponse
import models.taskList.TaskStatus
import models.{SessionData, TaskCategory, UserAnswers}
import org.apache.pekko.Done
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.http.Status.OK
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.TaskStatusQuery
import repositories.SessionRepository
import services.UserAnswersService
import viewmodels.govuk.SummaryListFluency

import scala.concurrent.Future

class TaskListControllerSpec
    extends AnyFreeSpec
    with SpecBase
    with SummaryListFluency
    with MockitoSugar {

  "TaskListController" - {

    "must return OK and NOT persist when no task statuses change" in {

      val app =
        applicationBuilder()
          .build()

      running(app) {
        val req = FakeRequest(GET, controllers.routes.TaskListController.onPageLoad().url)
        val res = route(app, req).value

        status(res) mustEqual OK
      }
    }

    "fromDashboard" - {
      "must redirect to TaskList onPageLoad and set new session data" in {
        val app =
          applicationBuilder()
            .build()

        running(app) {
          val req = FakeRequest(GET, controllers.routes.TaskListController.fromDashboard("transferId").url)
          val res = route(app, req).value

          status(res) mustBe SEE_OTHER
          redirectLocation(res) mustBe Some(controllers.routes.TaskListController.onPageLoad().url)
        }
      }
    }
  }
}
