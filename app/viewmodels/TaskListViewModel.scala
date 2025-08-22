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

import models.UserAnswers
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases._

object TaskListViewModel {

  def rows(userAnswers: UserAnswers)(implicit messages: Messages): Seq[TaskListItem] = {
    TaskJourneyViewModels.valuesWithoutSubmissionJourney.map { journey =>
      TaskTileViewModel(
        id       = journey.id,
        linkText = messages(journey.linkTextKey),
        link     = journey.entry(userAnswers),
        status   = journey.status(userAnswers)
      ).toTaskListItem
    }
  }

  def submissionRow(userAnswers: UserAnswers)(implicit messages: Messages): TaskListItem = {
    val journey = TaskJourneyViewModels.SubmissionDetailsJourneyViewModel
    TaskTileViewModel(
      id       = journey.id,
      linkText = messages(journey.linkTextKey),
      link     = journey.entry(userAnswers),
      status   = journey.status(userAnswers)
    ).toTaskListItem
  }
}
