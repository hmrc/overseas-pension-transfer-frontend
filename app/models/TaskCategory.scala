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

package models

sealed trait TaskCategory

object TaskCategory extends Enumerable.Implicits {

  case object MemberDetails          extends WithName("memberDetails") with TaskCategory
  case object TransferDetails        extends WithName("transferDetails") with TaskCategory
  case object QROPSDetails           extends WithName("qropsDetails") with TaskCategory
  case object SchemeManagerDetails   extends WithName("schemeManagerDetails") with TaskCategory
  case object SubmissionDetails      extends WithName("submissionDetails") with TaskCategory

  val values: Seq[TaskCategory] = Seq(
    MemberDetails,
    TransferDetails,
    QROPSDetails,
    SchemeManagerDetails,
    SubmissionDetails
  )

  implicit val enumerable: Enumerable[TaskCategory] =
    Enumerable(values.map(v => v.toString -> v): _*)
}
