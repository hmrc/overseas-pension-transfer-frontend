package viewmodels.checkAnswers

import controllers.routes
import models.{Mode, UserAnswers}
import pages.$className$Page
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.govuk.summarylist._
import viewmodels.implicits._

object $className$Summary  {

  def row(mode: Mode, answers: UserAnswers)(implicit messages: Messages): Option[SummaryListRow] =
    answers.get($className$Page).map {
      answer =>

        SummaryListRowViewModel(
          key     = "$className;format="decap"$.checkYourAnswersLabel",
          value   = ValueViewModel(HtmlFormat.escape(answer).toString),
          actions = Seq(
            ActionItemViewModel("site.change", routes.$className$Controller.onPageLoad(modeg).url)
              .withVisuallyHiddenText(messages("$className;format="decap"$.change.hidden"))
          )
        )
    }
}
