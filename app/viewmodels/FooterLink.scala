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
import play.api.i18n.Messages

/** Builds a list of footer links for a page. Priority of footers:
  *   - Start footer (only for StartPage)
  *   - CYA footer (for 4 CYA pages)
  *   - Page footer (default for most pages)
  */
case class FooterLink(id: String, href: String, text: String)

object FooterLink {

  def build(
      showCYAFooter: Boolean      = false,
      showStartFooter: Boolean    = false,
      showPageFooter: Boolean     = true,
      showTaskListFooter: Boolean = false
    )(implicit messages: Messages
    ): Seq[FooterLink] = {

    val dashboardLink = FooterLink(
      id   = "returnDashboardLink",
      href = routes.DashboardController.onPageLoad().url,
      text = messages("footer.link.text.dashboard")
    )

    val discardReportLink = FooterLink(
      id   = "discardReportLink",
      href = routes.DiscardTransferConfirmController.onPageLoad().url,
      text = messages("footer.link.text.discard.report")
    )

    val taskListLink = FooterLink(
      id   = "returnTaskListLink",
      href = routes.TaskListController.onPageLoad().url,
      text = messages("footer.link.text.tasklist")
    )

    (showStartFooter, showCYAFooter, showTaskListFooter, showPageFooter) match {
      case (true, _, _, _) | (_, true, _, _) => Seq(dashboardLink)
      case (_, _, true, _)                   => Seq(discardReportLink, dashboardLink)
      case (_, _, _, true)                   => Seq(taskListLink)
      case _                                 => Seq.empty
    }
  }
}
