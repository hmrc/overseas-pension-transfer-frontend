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

package navigators

import controllers.routes
import models.AllTransfersItem
import models.QtStatus.{Compiled, InProgress, Submitted}
import play.api.mvc.Call

object AllTransfersLinkNavigator {

  def linkFor(item: AllTransfersItem): Call =
    item.qtStatus match {
      case Some(InProgress)           =>
        val id   = item.transferReference.getOrElse(throw new IllegalArgumentException("In progress transfer must have transferReference id"))
        val pstr = item.pstrNumber.map(_.value).getOrElse(throw new IllegalArgumentException("In progress transfer must have pstrNumber"))
        routes.TaskListController.continueJourney(
          referenceId = id,
          pstr        = pstr,
          qtStatus    = InProgress.toString
        )
      case Some(Submitted | Compiled) =>
        val id      = item.qtReference.map(_.value).getOrElse(throw new IllegalArgumentException("Submitted transfer must have qtReference"))
        val pstr    = item.pstrNumber.map(_.value).getOrElse(throw new IllegalArgumentException("Submitted transfer must have pstrNumber"))
        val version = item.qtVersion.getOrElse(throw new IllegalArgumentException("Submitted transfer must have qtVersion"))
        routes.ViewSubmittedController.onPageLoad(id, pstr, Submitted.toString, version)
      case _                          => routes.JourneyRecoveryController.onPageLoad()
    }
}
