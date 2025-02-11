/*
 * Copyright 2024 HM Revenue & Customs
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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    case MemberNamePage            => _ => routes.MemberNinoController.onPageLoad(NormalMode)
    case MemberNinoPage            => _ => routes.MemberDateOfBirthController.onPageLoad(NormalMode)
    case MemberDoesNotHaveNinoPage => _ => routes.MemberDateOfBirthController.onPageLoad(NormalMode)
    // TODO other pages
    case _                         => _ => routes.IndexController.onPageLoad()
  }

  private val checkRouteMap: Page => UserAnswers => Boolean => Call = {
    case MemberHasANinoPage => _ => {
        case true  => routes.MemberNinoController.onPageLoad(NormalMode)
        case false => routes.MemberDoesNotHaveNinoController.onPageLoad(NormalMode)
      }
    // TODO other pages
    case _                  => _ => _ => routes.IndexController.onPageLoad()
  }

  def nextPage(
      page: Page,
      mode: Mode,
      userAnswers: UserAnswers,
      hasAnswerChanged: Boolean = true
    ): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(page)(userAnswers)(hasAnswerChanged)
  }
}
