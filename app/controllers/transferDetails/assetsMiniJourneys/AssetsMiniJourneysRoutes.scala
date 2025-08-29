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

  // ----- Cash -----

  val CashAmountInTransferController =
    cash.routes.CashAmountInTransferController

  // ----- Unquoted Shares -----

  val UnquotedSharesStartController =
    unquotedShares.routes.UnquotedSharesStartController

  val UnquotedSharesCompanyNameController =
    unquotedShares.routes.UnquotedSharesCompanyNameController

  val UnquotedSharesValueController =
    unquotedShares.routes.UnquotedSharesValueController

  val UnquotedSharesNumberController =
    unquotedShares.routes.UnquotedSharesNumberController

  val UnquotedSharesClassController =
    unquotedShares.routes.UnquotedSharesClassController

  val UnquotedSharesCYAController =
    unquotedShares.routes.UnquotedSharesCYAController

  val UnquotedSharesAmendContinueController =
    unquotedShares.routes.UnquotedSharesAmendContinueController

  val UnquotedSharesConfirmRemovalController =
    unquotedShares.routes.UnquotedSharesConfirmRemovalController

  // ----- Quoted Shares -----

  val QuotedSharesStartController =
    quotedShares.routes.QuotedSharesStartController

  val QuotedSharesCompanyNameController =
    quotedShares.routes.QuotedSharesCompanyNameController

  val QuotedSharesValueController =
    quotedShares.routes.QuotedSharesValueController

  val QuotedSharesNumberController =
    quotedShares.routes.QuotedSharesNumberController

  val QuotedSharesClassController =
    quotedShares.routes.QuotedSharesClassController

  val QuotedSharesCYAController =
    quotedShares.routes.QuotedSharesCYAController

  val QuotedSharesAmendContinueController =
    quotedShares.routes.QuotedSharesAmendContinueController

  val QuotedSharesConfirmRemovalController =
    quotedShares.routes.QuotedSharesConfirmRemovalController

  // ----- Property -----

  val PropertyStartController =
    property.routes.PropertyStartController

  val PropertyAddressController =
    property.routes.PropertyAddressController

  val PropertyDescriptionController =
    property.routes.PropertyDescriptionController

  val PropertyValueController =
    property.routes.PropertyValueController

  val PropertyCYAController =
    property.routes.PropertyCYAController

  val PropertyAmendContinueController =
    property.routes.PropertyAmendContinueController

  val PropertyConfirmRemovalController =
    property.routes.PropertyConfirmRemovalController

  // ----- Other Assets -----

  val OtherAssetsStartController =
    otherAssets.routes.OtherAssetsStartController

  val OtherAssetsDescriptionController =
    otherAssets.routes.OtherAssetsDescriptionController

  val OtherAssetsValueController =
    otherAssets.routes.OtherAssetsValueController

  val OtherAssetsCYAController =
    otherAssets.routes.OtherAssetsCYAController

  val OtherAssetsAmendContinueController =
    otherAssets.routes.OtherAssetsAmendContinueController

  val OtherAssetsConfirmRemovalController =
    otherAssets.routes.OtherAssetsConfirmRemovalController

}
