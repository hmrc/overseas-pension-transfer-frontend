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

package controllers.transferDetails.assetsMiniJourneys

object AssetsMiniJourneysRoutes {

  // ----- Unquoted Shares -----

  val UnquotedShareStartController =
    controllers.transferDetails.routes.UnquotedShareStartController

  val UnquotedShareCompanyNameController =
    controllers.transferDetails.routes.UnquotedShareCompanyNameController

  val UnquotedShareValueController =
    controllers.transferDetails.routes.UnquotedShareValueController

  val NumberOfUnquotedSharesController =
    controllers.transferDetails.routes.NumberOfUnquotedSharesController

  val UnquotedSharesClassController =
    controllers.transferDetails.routes.UnquotedSharesClassController

  val UnquotedShareCYAController =
    controllers.transferDetails.routes.UnquotedShareCYAController

  val UnquotedSharesAmendContinueController =
    controllers.transferDetails.routes.UnquotedSharesAmendContinueController

  val UnquotedSharesConfirmRemovalController =
    controllers.transferDetails.routes.UnquotedSharesConfirmRemovalController

  // ----- Quoted Shares -----

  val AddQuotedShareController =
    controllers.transferDetails.routes.AddQuotedShareController

  val QuotedShareCompanyNameController =
    controllers.transferDetails.routes.QuotedShareCompanyNameController

  val QuotedShareValueController =
    controllers.transferDetails.routes.QuotedShareValueController

  val NumberOfQuotedSharesController =
    controllers.transferDetails.routes.NumberOfQuotedSharesController

  val ClassOfQuotedSharesController =
    controllers.transferDetails.routes.ClassOfQuotedSharesController

  val QuotedShareCYAController =
    controllers.transferDetails.routes.QuotedShareCYAController

  val QuotedSharesAmendContinueController =
    controllers.transferDetails.routes.QuotedSharesAmendContinueController
}
