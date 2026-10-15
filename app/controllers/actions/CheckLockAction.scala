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

package controllers.actions

import utils.AppUtils
import play.api.mvc.{ActionRefiner, Result}
import controllers.routes
import play.api.Logging
import play.api.mvc.Results.Redirect
import repositories.{ExpiringMongoLockRepository, SessionRepository}
import models.requests.SchemeRequest

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import javax.inject.Inject

class CheckLockActionImpl @Inject() (
  sessionRepository: SessionRepository,
  lockRepository: ExpiringMongoLockRepository
)(implicit val executionContext: ExecutionContext)
    extends CheckLockAction
    with AppUtils
    with Logging {

  override protected def refine[A](request: SchemeRequest[A]): Future[Either[Result, SchemeRequest[A]]] =
    sessionRepository.get(request.authenticatedUser.internalId) flatMap {
      case Some(value) =>
        val lockId = value.transferId.value
        val owner  = request.authenticatedUser.owner()

        for {
          refreshedLock <-
            lockRepository.refreshExpiry(lockId, owner) // Succeeds if I own the lock and not expired
          owned         <- if (!refreshedLock) {
                             lockRepository
                               .takeLock(lockId, owner)
                               .map(
                                 _.fold(false)(_ => true)
                               ) // Succeeds if the lock has expired/missing, else lock is live and not the owner
                           } else {
                             Future.successful(true) // Taken the lock during the refresh
                           }
        } yield
          if (owned) {
            Right(request)
          } else {
            Left(Redirect(routes.JourneyRecoveryController.onPageLoad()))
          }
      case None        =>
        logger.error("No Session Data found")
        Future.successful(Left(Redirect(routes.JourneyRecoveryController.onPageLoad())))
    }
}

trait CheckLockAction extends ActionRefiner[SchemeRequest, SchemeRequest]
