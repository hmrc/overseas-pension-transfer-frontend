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

package viewmodels

case class SearchBarViewModel(
    id: String                 = "dashboard-search",
    name: String               = "search",
    label: String,
    action: String,
    buttonText: Option[String] = None,
    value: Option[String]      = None,
    hint: Option[String]       = None,
    clearUrl: Option[String]   = None
  )
