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

  def build(showCYAFooter: Boolean = false, showStartFooter: Boolean = false, showPageFooter: Boolean = true)(implicit messages: Messages): Seq[FooterLink] = {
    val links = Seq.newBuilder[FooterLink]

    val dashboardLink = FooterLink(
      id   = "returnDashboardLink",
      href = routes.IndexController.onPageLoad().url, // TODO Index to be as Dashboard once implemented
      text = messages("footer.link.text.dashboard")
    )

    if (showStartFooter) {
      links += dashboardLink
    } else if (showCYAFooter) {
      links += FooterLink(
        id   = "discardReportLink",
        href = routes.DiscardTransferConfirmController.onPageLoad().url,
        text = messages("footer.link.text.discard.report")
      )
      links += dashboardLink
    } else if (showPageFooter) {
      links += FooterLink(
        id   = "returnTaskListLink",
        href = "", // TODO routes.TaskListController.onPageLoad().url once implemented
        text = messages("footer.link.text.tasklist")
      )
    }
    links.result()
  }
}
