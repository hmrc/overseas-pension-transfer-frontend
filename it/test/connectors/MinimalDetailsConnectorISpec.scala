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

package connectors

import base.ISpecBase
import models.MinimalDetails
import play.api.libs.json.Json
import play.api.test.Helpers.*
import stubs.MinimalDetailsStub

import scala.concurrent.ExecutionContext.Implicits.global

class MinimalDetailsConnectorISpec extends ISpecBase {

  val connector: MinimalDetailsConnector = app.injector.instanceOf[MinimalDetailsConnector]

  "MinimalDetailsConnector.fetch" when {
    "fetching minimal details" must {
      "return Right(MinimalDetails) for PSA route" in {
        MinimalDetailsStub.psaSuccess(psaId.value, Json.stringify(Json.toJson(minimalDetailsIndividual)))

        await(connector.fetch(psaId)) mustBe Right(minimalDetailsIndividual)
      }

      "return Right(MinimalDetails) for PSP route" in {
        MinimalDetailsStub.pspSuccess(pspId.value, Json.stringify(Json.toJson(minimalDetailsIndividual)))

        await(connector.fetch(pspId)) mustBe Right(minimalDetailsIndividual)
      }

      "return Left(DetailsNotFound) when 404 returned containing 'no match found'" in {
        MinimalDetailsStub.psaNotFoundWithNoMatch(psaId.value)

        await(connector.fetch(psaId)) mustBe Left(DetailsNotFound)
      }

      "return Left(UpstreamError) when other upstream errors occur" in {
        MinimalDetailsStub.psaForbiddenDelimited(psaId.value)

        await(connector.fetch(psaId)) mustBe Left(UpstreamError)
      }
    }
  }
}
