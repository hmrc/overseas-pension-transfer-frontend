package pages

import controllers.routes
import models.UserAnswers
import play.api.libs.json.JsPath
import play.api.mvc.Call

case object IsTransferCashOnlyPage extends QuestionPage[Boolean] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "isTransferCashOnly"

  override protected def nextPageNormalMode(answers: UserAnswers): Call =
    routes.IndexController.onPageLoad()
}
