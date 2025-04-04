package pages

import controllers.routes
import models.UserAnswers
import play.api.mvc.Call
import play.api.libs.json.JsPath

import java.time.LocalDate

case object $className$Page extends QuestionPage[LocalDate] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.IndexController.onPageLoad()
}
