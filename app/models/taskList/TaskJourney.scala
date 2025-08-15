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

package models.taskList

import models.taskList.TaskStatus.CannotStart
import models.{Mode, NormalMode, TaskCategory, UserAnswers}
import play.api.mvc.Call
import queries.TaskStatusQuery

trait TaskJourney {
  def category: TaskCategory
  def id: String
  def linkTextKey: String
  def hint: Option[String] = None

  def start(mode: Mode): Call
  def cya(): Call

  final def status(ua: UserAnswers): TaskStatus =
    ua.get(TaskStatusQuery(category)).getOrElse(CannotStart)

  final def entry(ua: UserAnswers): Call =
    status(ua) match {
      case TaskStatus.Completed => cya()
      case _                    => start(NormalMode)
    }
}

object TaskJourneys {

  case object MemberDetailsJourney extends TaskJourney {
    val category    = TaskCategory.MemberDetails
    val id          = "member-details"
    val linkTextKey = "taskList.memberDetails.linkText"

    def start(m: Mode): Call = controllers.memberDetails.routes.MemberNameController.onPageLoad(m)
    def cya(): Call          = controllers.memberDetails.routes.MemberDetailsCYAController.onPageLoad()
  }

  case object TransferDetailsJourney extends TaskJourney {
    val category    = TaskCategory.TransferDetails
    val id          = "transfer-details"
    val linkTextKey = "taskList.transferDetails.linkText"

    def start(m: Mode): Call = controllers.transferDetails.routes.OverseasTransferAllowanceController.onPageLoad(m)
    def cya(): Call          = controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad()
  }

  case object QropsDetailsJourney extends TaskJourney {
    val category    = TaskCategory.QROPSDetails
    val id          = "qrops-details"
    val linkTextKey = "taskList.qropsDetails.linkText"

    def start(m: Mode): Call = controllers.qropsDetails.routes.QROPSNameController.onPageLoad(m)
    def cya(): Call          = controllers.qropsDetails.routes.QROPSDetailsCYAController.onPageLoad()
  }

  case object SchemeManagerDetailsJourney extends TaskJourney {
    val category             = TaskCategory.SchemeManagerDetails
    val id                   = "scheme-manager-details"
    val linkTextKey          = "taskList.schemeManagerDetails.linkText"
    def start(m: Mode): Call = controllers.qropsSchemeManagerDetails.routes.SchemeManagersNameController.onPageLoad(m)
    def cya(): Call          = controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onPageLoad()
  }

  case object SubmissionDetailsJourney extends TaskJourney {
    val category             = TaskCategory.SubmissionDetails
    val id                   = "submit"
    val linkTextKey          = "taskList.submit.linkText"
    def start(m: Mode): Call = controllers.routes.IndexController.onPageLoad()
    def cya(): Call          = controllers.routes.IndexController.onPageLoad()
  }

  val values: Seq[TaskJourney] = Seq(
    MemberDetailsJourney,
    TransferDetailsJourney,
    QropsDetailsJourney,
    SchemeManagerDetailsJourney,
    SubmissionDetailsJourney
  )

  val valuesWithoutSubmissionJourney: Seq[TaskJourney] =
    values.filterNot(_ == SubmissionDetailsJourney)
}
