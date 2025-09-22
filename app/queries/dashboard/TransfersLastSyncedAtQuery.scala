package queries.dashboard

import play.api.libs.json.JsPath
import queries.{Gettable, Settable}

import java.time.Instant

case object TransfersLastSyncedAtQuery extends Gettable[Instant] with Settable[Instant]{
  override def path: JsPath = JsPath \ "transfers" \ "lastUpdated"
}
