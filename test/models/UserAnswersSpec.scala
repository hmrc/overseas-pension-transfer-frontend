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

import base.SpecBase
import models.assets.{QuotedSharesEntry, UnquotedSharesEntry}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json._
import queries.assets.{QuotedSharesQuery, UnquotedSharesQuery}

import java.time.Instant

class UserAnswersSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val instant = Instant.now()

  "buildMinimal" - {

    "must return a new UserAnswers with only the selected query's data" in {
      val fullData = Json.obj(
        "transferDetails" -> Json.obj(
          "unquotedShares" -> Json.arr(
            Json.obj(
              UnquotedSharesEntry.CompanyName    -> "ABC Ltd",
              UnquotedSharesEntry.ValueOfShares  -> 1000,
              UnquotedSharesEntry.NumberOfShares -> 10,
              UnquotedSharesEntry.ClassOfShares  -> "Ordinary"
            )
          ),
          "quotedShares"   -> Json.arr(
            Json.obj(
              QuotedSharesEntry.CompanyName    -> "XYZ Plc",
              QuotedSharesEntry.ValueOfShares  -> 2000,
              QuotedSharesEntry.NumberOfShares -> 20,
              QuotedSharesEntry.ClassOfShares  -> "Preferred"
            )
          )
        )
      )

      val original = UserAnswers("id", fullData, instant)

      val result = UserAnswers.buildMinimal(original, UnquotedSharesQuery)

      result.isSuccess mustBe true
      val minimal = result.get

      minimal.id mustBe original.id
      minimal.lastUpdated mustBe original.lastUpdated

      (minimal.data \ "transferDetails" \ "unquotedShares").asOpt[List[UnquotedSharesEntry]] mustBe
        (original.data \ "transferDetails" \ "unquotedShares").asOpt[List[UnquotedSharesEntry]]

      (minimal.data \ "transferDetails" \ "quotedShares").asOpt[List[UnquotedSharesEntry]] mustBe empty
    }

    "must return Failure if the original UserAnswers does not contain the query value" in {
      val empty = UserAnswers("id", Json.obj(), instant)

      val result = UserAnswers.buildMinimal(empty, QuotedSharesQuery)

      result.isFailure mustBe true
    }
  }
}
