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

import scala.util.matching.Regex

object TransferSearch {

  def filterTransfers(
      transfers: Seq[AllTransfersItem],
      rawTerm: String
    ): Seq[AllTransfersItem] = {

    val term = rawTerm.trim

    if (term.isEmpty) {
      transfers
    } else {
      TransferSearch.detectSearchMode(term) match {
        case QtRefSearch(normalisedRef) =>
          filterByQtRef(transfers, normalisedRef)

        case NinoSearch(normalisedNino) =>
          filterByNino(transfers, normalisedNino)

        case NameSearch(_, parts) =>
          filterByName(transfers, parts)
      }
    }
  }

  private val NinoRegex: Regex = "^[A-Z]{2}[0-9]{6}[A-Z]$".r

  private def normaliseForId(s: String): String =
    s.replaceAll("\\s+", "").toUpperCase

  private def detectSearchMode(rawTerm: String): SearchMode = {
    val trimmed    = rawTerm.trim
    val normalised = normaliseForId(trimmed)

    if (QtNumber.regex.pattern.matcher(normalised).matches()) {
      QtRefSearch(normalised)
    } else if (NinoRegex.pattern.matcher(normalised).matches()) {
      NinoSearch(normalised)
    } else {
      val parts = trimmed.split("\\s+").toList.filter(_.nonEmpty)
      NameSearch(trimmed, parts)
    }
  }

  private def filterByQtRef(
      transfers: Seq[AllTransfersItem],
      normalisedRef: String
    ): Seq[AllTransfersItem] = {

    def normaliseRef(value: String): String =
      TransferSearch.normaliseForId(value)

    transfers.filter { t =>
      t.transferId match {
        case QtNumber(ref) =>
          normaliseRef(ref) == normalisedRef
        case _             =>
          false
      }
    }
  }

  private def filterByNino(
      transfers: Seq[AllTransfersItem],
      normalisedNino: String
    ): Seq[AllTransfersItem] = {

    def normaliseNino(n: String): String =
      TransferSearch.normaliseForId(n)

    transfers.filter { t =>
      t.nino
        .map(normaliseNino)
        .contains(normalisedNino)
    }
  }

  private def filterByName(
      transfers: Seq[AllTransfersItem],
      parts: List[String]
    ): Seq[AllTransfersItem] = {

    def norm(s: String): String = s.trim.toLowerCase

    val normParts = parts.map(norm)

    transfers.filter { t =>
      val firstOpt = t.memberFirstName.map(norm)
      val lastOpt  = t.memberSurname.map(norm)

      normParts match {
        case single :: Nil =>
          firstOpt.contains(single) || lastOpt.contains(single)

        case first :: rest =>
          val last = rest.last
          firstOpt.contains(first) && lastOpt.contains(last)

        case Nil =>
          false
      }
    }
  }
}

sealed private trait SearchMode
private case class QtRefSearch(normalisedRef: String)               extends SearchMode
private case class NinoSearch(normalisedNino: String)               extends SearchMode
private case class NameSearch(rawTerm: String, parts: List[String]) extends SearchMode
