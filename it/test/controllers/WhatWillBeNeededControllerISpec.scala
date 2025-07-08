package controllers

import base.BaseISpec
import play.api.test.Injecting

class WhatWillBeNeededControllerISpec extends BaseISpec with Injecting {

  "onPageLoad" should {
    "return OK with view and" should {
      "populate session repository with save for later data returned from the backend service" in {

      }

      "populate session repository with userId and blank Json record when no save for later data is returned" in {
        
      }
    }
  }

}
