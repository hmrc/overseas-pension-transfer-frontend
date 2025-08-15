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

package viewmodels

import controllers.routes
import models.TaskCategory.{MemberDetails, QROPSDetails, SchemeManagerDetails, SubmissionDetails, TransferDetails}
import models.taskList.TaskStatus.CannotStart
import models.{TaskViewModel, UserAnswers}
import play.api.i18n.Messages
import queries.TaskStatusQuery
import uk.gov.hmrc.govukfrontend.views.Aliases._

object TaskListViewModel {

  def rows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[TaskListItem] =
    Seq(
      TaskViewModel(
        "member-details",
        "The member details",
        "Tell us about the member you made the transfer for",
        routes.IndexController.onPageLoad(),
        userAnswers.get(TaskStatusQuery(MemberDetails)).getOrElse(CannotStart)
      ).toTaskListItem,
      TaskViewModel(
        "transfer-details",
        "The transfer details",
        "Tell us more information about the overseas transfer",
        routes.IndexController.onPageLoad(),
        userAnswers.get(TaskStatusQuery(TransferDetails)).getOrElse(CannotStart)
      ).toTaskListItem,
      TaskViewModel(
        "qrops-details",
        "The QROPS details",
        "Tell us more information about the QROPS",
        routes.IndexController.onPageLoad(),
        userAnswers.get(TaskStatusQuery(QROPSDetails)).getOrElse(CannotStart)
      ).toTaskListItem,
      TaskViewModel(
        "scheme-manager-details",
        "The QROPS scheme manager details",
        "Tell us more about the QROPS scheme manager",
        routes.IndexController.onPageLoad(),
        userAnswers.get(TaskStatusQuery(SchemeManagerDetails)).getOrElse(CannotStart)
      ).toTaskListItem,
      TaskViewModel(
        "submit",
        "Submit your form",
        "Check all your answers and submit the form",
        routes.IndexController.onPageLoad(),
        userAnswers.get(TaskStatusQuery(SubmissionDetails)).getOrElse(CannotStart)
      ).toTaskListItem
    )
}
