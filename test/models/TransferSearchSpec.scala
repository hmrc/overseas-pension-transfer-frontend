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

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec

class TransferSearchSpec extends AnyFreeSpec with SpecBase {

  private def withId(id: TransferId): AllTransfersItem =
    transferItem.copy(transferId = id)

  private def withNino(nino: Option[String]): AllTransfersItem =
    transferItem.copy(nino = nino)

  private def withName(first: Option[String], last: Option[String]): AllTransfersItem =
    transferItem.copy(memberFirstName = first, memberSurname = last)

  "TransferSearch.filterTransfers" - {

    "must return the original sequence when the term is empty or whitespace" in {
      val transfers = Seq(
        transferItem,
        withId(testQtNumber)
      )

      TransferSearch.filterTransfers(transfers, "") mustBe transfers
      TransferSearch.filterTransfers(transfers, "   ") mustBe transfers
    }

    "must handle QT reference search with dif cases and with space" in {
      val qt1           = withId(QtNumber("QT123456"))
      val qt2           = withId(QtNumber("QT999999"))
      val transferIdRef = withId(TransferNumber("QT123456"))

      val transfers = Seq(qt1, qt2, transferIdRef)

      TransferSearch.filterTransfers(transfers, "QT123456") mustBe Seq(qt1)
      TransferSearch.filterTransfers(transfers, "qt123456") mustBe Seq(qt1)
      TransferSearch.filterTransfers(transfers, "QT 12 34 56") mustBe Seq(qt1)
      TransferSearch.filterTransfers(transfers, "QT999999") mustBe Seq(qt2)
    }

    "must handle NINO search with dif cases and with space" in {
      val a = withNino(Some("AA123456A"))
      val b = withNino(Some("BB123456B"))
      val c = withNino(None)

      val transfers = Seq(a, b, c)

      TransferSearch.filterTransfers(transfers, "AA123456A") mustBe Seq(a)
      TransferSearch.filterTransfers(transfers, "aa123456a") mustBe Seq(a)
      TransferSearch.filterTransfers(transfers, "AA 12 34 56 A") mustBe Seq(a)
      TransferSearch.filterTransfers(transfers, "BB123456B") mustBe Seq(b)
      TransferSearch.filterTransfers(transfers, "CC123456C") mustBe Seq.empty
    }

    "must treat a non QT, non NINO term as a name search with a single token (matching first OR last name, case-insensitive)" in {
      val johnDoe   = withName(Some("John"), Some("Doe"))
      val janeSmith = withName(Some("Jane"), Some("Smith"))
      val transfers = Seq(johnDoe, janeSmith)

      TransferSearch.filterTransfers(transfers, "john") mustBe Seq(johnDoe)
      TransferSearch.filterTransfers(transfers, "doe") mustBe Seq(johnDoe)
      TransferSearch.filterTransfers(transfers, "JoHn") mustBe Seq(johnDoe)
      TransferSearch.filterTransfers(transfers, "  john  ") mustBe Seq(johnDoe)
      TransferSearch.filterTransfers(transfers, "smith") mustBe Seq(janeSmith)
    }

    "must treat a two-token name search as first and last name, exact match, case-insensitive" in {
      val johnDoe   = withName(Some("John"), Some("Doe"))
      val johnSmith = withName(Some("John"), Some("Smith"))
      val aliceDoe  = withName(Some("Alice"), Some("Doe"))
      val transfers = Seq(johnDoe, johnSmith, aliceDoe)

      TransferSearch.filterTransfers(transfers, "John Doe") mustBe Seq(johnDoe)
      TransferSearch.filterTransfers(transfers, "john doe") mustBe Seq(johnDoe)
      TransferSearch.filterTransfers(transfers, "John Smith") mustBe Seq(johnSmith)
    }

    "must support multi-token names by trying all possible first/last splits and matching exact first AND last names" in {
      val compoundName =
        withName(Some("John Marie"), Some("David Scott")).copy(transferId = QtNumber("QT000001"))

      val simpleName =
        withName(Some("John"), Some("Doe")).copy(transferId = QtNumber("QT000002"))

      val transfers = Seq(compoundName, simpleName)

      TransferSearch.filterTransfers(transfers, "John Marie David Scott") mustBe Seq(compoundName)
    }

    "must ignore middle tokens when they do not form a valid split matching first and last names" in {
      val person = withName(Some("John"), Some("Doe")).copy(transferId = QtNumber("QT000001"))

      val transfers = Seq(person)

      TransferSearch.filterTransfers(transfers, "John X Doe") mustBe Seq.empty
    }

    "must return an empty result for a name search when there are no first or last names stored" in {
      val noNames = withName(None, None)

      TransferSearch.filterTransfers(Seq(noNames), "John") mustBe Seq.empty
    }

    "must preserve the original ordering of transfers when filtering by name" in {
      val a = withName(Some("John"), Some("Doe")).copy(transferId = QtNumber("QT000001"))
      val b = withName(Some("John"), Some("Smith")).copy(transferId = QtNumber("QT000002"))
      val c = withName(Some("John"), Some("Jones")).copy(transferId = QtNumber("QT000003"))

      val transfers = Seq(a, b, c)

      TransferSearch.filterTransfers(transfers, "John") mustBe Seq(a, b, c)
    }

    "must match correctly when term has surrounding whitespace for QT ref" in {
      val qtItem = withId(QtNumber("QT123456"))

      val transfers = Seq(qtItem)

      TransferSearch.filterTransfers(transfers, "  QT123456  ") mustBe Seq(qtItem)
    }

    "must match correctly when term has surrounding whitespace for NINO" in {
      val ninoItem = withNino(Some("AA123456A"))

      val transfers = Seq(ninoItem)

      TransferSearch.filterTransfers(transfers, "  AA123456A  ") mustBe Seq(ninoItem)
    }

    "must match correctly when term has surrounding whitespace for name" in {
      val nameItem = withName(Some("Jane"), Some("Smith"))

      val transfers = Seq(nameItem)

      TransferSearch.filterTransfers(transfers, "  Jane Smith  ") mustBe Seq(nameItem)
    }
  }
}
