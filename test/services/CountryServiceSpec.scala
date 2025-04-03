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

package services

import base.SpecBase
import models.address.Country
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment

import java.io.ByteArrayInputStream

class CountryServiceSpec extends SpecBase with MockitoSugar {

  private val countriesJsonPath = "public/countries.json"

  ".countries" - {

    "load and sort the countries from the JSON file" in {
      val mockEnv     = mock[Environment]
      val countryJson =
        """[
          |  { "country": "Canada", "code": "CA" },
          |  { "country": "United Kingdom", "code": "GB" },
          |  { "country": "France", "code": "FR" }
          |]""".stripMargin

      val jsonStream = new ByteArrayInputStream(countryJson.getBytes("UTF-8"))

      when(mockEnv.resourceAsStream(countriesJsonPath)).thenReturn(Some(jsonStream))

      val service = new CountryService(mockEnv)

      val loadedCountries = service.countries

      loadedCountries.map(_.name) mustBe Seq("Canada", "France", "United Kingdom")
      loadedCountries.map(_.code) mustBe Seq("CA", "FR", "GB")
    }

    "throw an exception if the JSON cannot be loaded" in {
      val mockEnv = mock[Environment]
      when(mockEnv.resourceAsStream(countriesJsonPath)).thenReturn(None)

      assertThrows[RuntimeException] {
        new CountryService(mockEnv).countries
      }
    }

    "throw an exception if the JSON structure is invalid" in {
      val mockEnv     = mock[Environment]
      val invalidJson =
        """[
          |  { "countryX": "Missing required fields" }
          |]""".stripMargin

      val jsonStream = new ByteArrayInputStream(invalidJson.getBytes("UTF-8"))
      when(mockEnv.resourceAsStream(countriesJsonPath)).thenReturn(Some(jsonStream))

      assertThrows[RuntimeException] {
        new CountryService(mockEnv).countries
      }
    }
  }

  ".find" - {

    "return Some(country) if the code exists" in {

      val mockEnv     = mock[Environment]
      val countryJson =
        """[
          |  { "country": "Bermuda", "code": "BM" },
          |  { "country": "United Kingdom", "code": "GB" }
          |]""".stripMargin

      when(mockEnv.resourceAsStream(countriesJsonPath))
        .thenReturn(Some(new ByteArrayInputStream(countryJson.getBytes("UTF-8"))))

      val service = new CountryService(mockEnv)
      val result  = service.find("GB")

      result mustBe Some(Country("GB", "United Kingdom"))
    }

    "return None if the code does not exist" in {

      val mockEnv     = mock[Environment]
      val countryJson =
        """[
          |  { "country": "Bermuda", "code": "BM" }
          |]""".stripMargin

      when(mockEnv.resourceAsStream(countriesJsonPath))
        .thenReturn(Some(new ByteArrayInputStream(countryJson.getBytes("UTF-8"))))

      val service = new CountryService(mockEnv)
      val result  = service.find("GB")

      result mustBe None
    }
  }
}
