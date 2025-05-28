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

import models.address.Country
import play.api.Environment
import play.api.libs.functional.syntax._
import play.api.libs.json._

import javax.inject.{Inject, Singleton}
import scala.io.Source

@Singleton
class CountryService @Inject() (env: Environment) {
  private val countriesJsonPath = "public/countries.json"

  lazy val countries: Seq[Country] = loadCountries()

  def find(code: String): Option[Country] = {
    countries.find(_.code == code)
  }

  private def getJson: Option[JsValue] =
    env.resourceAsStream(countriesJsonPath).map { stream =>
      Json.parse(Source.fromInputStream(stream).mkString)
    }

  implicit private val nameCodeReads: Reads[(String, String)] = (
    (JsPath \ "country").read[String] and
      (JsPath \ "code").read[String]
  ).tupled

  private def loadCountries(): Seq[Country] = {
    getJson match {
      case None       =>
        throw new RuntimeException("Couldn't load country data")
      case Some(json) =>
        json.validate[Seq[(String, String)]] match {
          case JsSuccess(countryPairs, _) =>
            countryPairs
              .map { case (countryName, code) =>
                Country(code, countryName)
              }
              .sortBy(_.name)
          case JsError(errors)            =>
            throw new RuntimeException(s"Failed to parse countries JSON: $errors")
        }
    }
  }
}
