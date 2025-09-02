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

package controllers.helpers

import play.api.Logging
import play.api.mvc.{RequestHeader, Result, Results}
import play.api.routing.Router

trait ErrorHandling extends Logging {

  protected def onFailureRedirect(err: Any)(implicit rh: RequestHeader): Result = {
    val (controller, method) = rh.attrs.get(Router.Attrs.HandlerDef)
      .map(hd => (hd.controller, hd.method))
      .getOrElse(("UnknownController", "UnknownMethod"))

    logger.warn(s"[$controller.$method] downstream persistence error: $err")
    Results.Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())
  }
}
