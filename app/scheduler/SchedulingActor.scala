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

package scheduler

import utils.ScheduledService
import scheduler.SchedulingActor._
import org.apache.pekko.actor.Actor
import org.apache.pekko.actor.ActorLogging
import org.apache.pekko.actor.Props
import play.api.Logging

class SchedulingActor extends Actor with ActorLogging with Logging {
  import context.dispatcher

  override def receive: Receive = { case message: ScheduledMessage[_] =>
    logger.info(s"Received ${message.getClass.getSimpleName}")
    message.service.invoke: Unit
  }
}

object SchedulingActor {

  sealed trait ScheduledMessage[A] {
    val service: ScheduledService[A]
  }

  case class LockCleanup(service: ScheduledService[Unit]) extends ScheduledMessage[Unit]

  def props: Props = Props[SchedulingActor]()
}
