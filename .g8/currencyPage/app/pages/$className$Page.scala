package pages

import controllers.routes
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object $className$Page extends QuestionPage[BigDecimal] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "$className;format="decap"$"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.IndexController.onPageLoad()
}