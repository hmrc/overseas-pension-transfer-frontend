package queries

import models.PensionSchemeDetails
import play.api.libs.json.JsPath

object PensionSchemeDetailsQuery extends Gettable[PensionSchemeDetails] with Settable[PensionSchemeDetails] {
  override val path: JsPath = JsPath \ "pensionSchemeDetails"
}
