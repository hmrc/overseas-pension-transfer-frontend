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

  val UnquotedSharesStartController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesStartController

  val UnquotedSharesCompanyNameController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesCompanyNameController

  val UnquotedSharesValueController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesValueController

  val UnquotedSharesNumberController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesNumberController

  val UnquotedSharesClassController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesClassController

  val UnquotedSharesCYAController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesCYAController

  val UnquotedSharesAmendContinueController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesAmendContinueController

  val UnquotedSharesConfirmRemovalController =
    controllers.transferDetails.assetsMiniJourneys.unquotedShares.routes.UnquotedSharesConfirmRemovalController

  // ----- Quoted Shares -----

  val QuotedSharesStartController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesStartController

  val QuotedSharesCompanyNameController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesCompanyNameController

  val QuotedSharesValueController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesValueController

  val QuotedSharesNumberController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesNumberController

  val QuotedSharesClassController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesClassController

  val QuotedSharesCYAController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesCYAController

  val QuotedSharesAmendContinueController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesAmendContinueController

  val QuotedSharesConfirmRemovalController =
    controllers.transferDetails.assetsMiniJourneys.quotedShares.routes.QuotedSharesConfirmRemovalController

  // ----- Property -----

  val PropertyStartController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyStartController

  val PropertyAddressController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyAddressController

  val PropertyDescriptionController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyDescriptionController

  val PropertyValueController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyValueController

  val PropertyCYAController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyCYAController

  val PropertyAmendContinueController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyAmendContinueController

  val PropertyConfirmRemovalController =
    controllers.transferDetails.assetsMiniJourneys.property.routes.PropertyConfirmRemovalController

  // ----- Other Assets -----

  val OtherAssetsStartController =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsStartController

  val OtherAssetsDescriptionController =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsDescriptionController

  val OtherAssetsValueController =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsValueController

  val OtherAssetsCYAController =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsCYAController

  val OtherAssetsAmendContinueController =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsAmendContinueController

  val OtherAssetsConfirmRemovalController =
    controllers.transferDetails.assetsMiniJourneys.otherAssets.routes.OtherAssetsConfirmRemovalController

}
