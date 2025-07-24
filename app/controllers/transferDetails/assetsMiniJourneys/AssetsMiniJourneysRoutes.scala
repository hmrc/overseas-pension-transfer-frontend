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
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedShareStartController

  val UnquotedShareCompanyNameController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedShareCompanyNameController

  val UnquotedShareValueController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedShareValueController

  val NumberOfUnquotedSharesController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.NumberOfUnquotedSharesController

  val UnquotedSharesClassController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesClassController

  val UnquotedShareCYAController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedShareCYAController

  val UnquotedSharesAmendContinueController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesAmendContinueController

  val UnquotedSharesConfirmRemovalController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesConfirmRemovalController

  // ----- Quoted Shares -----

  val AddQuotedShareController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.AddQuotedShareController

  val QuotedShareCompanyNameController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedShareCompanyNameController

  val QuotedShareValueController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedShareValueController

  val NumberOfQuotedSharesController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.NumberOfQuotedSharesController

  val ClassOfQuotedSharesController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.ClassOfQuotedSharesController

  val QuotedShareCYAController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedShareCYAController

  val QuotedSharesAmendContinueController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesAmendContinueController
}
