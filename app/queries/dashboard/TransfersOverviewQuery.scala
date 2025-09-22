package queries.dashboard

import models.AllTransfersItem
import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

case object TransfersOverviewQuery extends Gettable[Seq[AllTransfersItem]] with Settable[Seq[AllTransfersItem]] {
  override def path: JsPath = JsPath \ "transfers" \ "data"
}
