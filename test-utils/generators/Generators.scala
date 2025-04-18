/*
 * Copyright 2024 HM Revenue & Customs
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

package generators

import java.time.{Instant, LocalDate, ZoneOffset}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen._
import org.scalacheck.{Gen, Shrink}
import play.api.Logging
import wolfendale.scalacheck.regexp.RegexpGen

trait Generators extends ModelGenerators with Logging {

  implicit val dontShrink: Shrink[String] = Shrink.shrinkAny

  private val validCharacters: Seq[Char] =
    ('A' to 'Z') ++
      ('a' to 'z') ++
      ('À' to 'Ö') ++
      ('Ø' to 'ö') ++
      ('ø' to 'ÿ')

  private val invalidCharacters: Seq[Char] = Seq('<', '>', '=', '|', '^')

  def genValidChar: Gen[Char] =
    Gen.oneOf(validCharacters)

  def genInvalidChar: Gen[Char] =
    Gen.oneOf(invalidCharacters)

  def genIntersperseString(gen: Gen[String], value: String, frequencyV: Int = 1, frequencyN: Int = 10): Gen[String] = {

    val genValue: Gen[Option[String]] = Gen.frequency(frequencyN -> None, frequencyV -> Gen.const(Some(value)))

    for {
      seq1 <- gen
      seq2 <- Gen.listOfN(seq1.length, genValue)
    } yield {
      seq1.toSeq.zip(seq2).foldLeft("") {
        case (acc, (n, Some(v))) =>
          acc + n + v
        case (acc, (n, _))       =>
          acc + n
      }
    }
  }

  def intsInRangeWithCommas(min: Int, max: Int): Gen[String] = {
    val numberGen = choose[Int](min, max).map(_.toString)
    genIntersperseString(numberGen, ",")
  }

  def intsLargerThanMaxValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x > Int.MaxValue)

  def intsSmallerThanMinValue: Gen[BigInt] =
    arbitrary[BigInt] suchThat (x => x < Int.MinValue)

  def nonNumerics: Gen[String] =
    alphaStr suchThat (_.size > 0)

  def decimals: Gen[String] =
    arbitrary[BigDecimal]
      .suchThat(_.abs < Int.MaxValue)
      .suchThat(!_.isValidInt)
      .map("%f".format(_))

  def intsBelowValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ < value)

  def intsAboveValue(value: Int): Gen[Int] =
    arbitrary[Int] suchThat (_ > value)

  def intsOutsideRange(min: Int, max: Int): Gen[Int] =
    arbitrary[Int] suchThat (x => x < min || x > max)

  def nonBooleans: Gen[String] =
    arbitrary[String]
      .suchThat(_.nonEmpty)
      .suchThat(_ != "true")
      .suchThat(_ != "false")

  def nonEmptyString: Gen[String] =
    stringOf(genValidChar) suchThat (_.nonEmpty)

  def stringsWithMaxLength(maxLength: Int): Gen[String] =
    for {
      length <- choose(1, maxLength)
      chars  <- listOfN(length, genValidChar)
    } yield chars.mkString

  def validEmailOfMaxLength(maxLength: Int): Gen[String] = {
    val domain          = "@example.com"
    val domainLength    = domain.length
    val localPartLength = maxLength - domainLength

    for {
      localChars <- Gen.listOfN(localPartLength, Gen.alphaNumChar)
    } yield localChars.mkString + domain
  }

  def stringsMatchingRegex(
      regex: String,
      maybeMinLength: Option[Int] = None,
      maybeMaxLength: Option[Int] = Some(99)
    ): Gen[String] = {

    val baseGen: Gen[String] = RegexpGen.from(regex)

    val lengthFilteredGen: Gen[String] =
      baseGen suchThat { s =>
        maybeMinLength.forall(min => s.length >= min) &&
        maybeMaxLength.forall(max => s.length <= max)
      }

    lengthFilteredGen retryUntil (_.matches(regex))
  }

  def stringsWithInvalidCharacters(
      maybeMinLength: Option[Int] = None,
      maybeMaxLength: Option[Int] = None
    ): Gen[String] = {
    val minLen = maybeMinLength.getOrElse(1)
    val maxLen = maybeMaxLength.getOrElse(100)

    for {
      length <- Gen.chooseNum(minLen, maxLen)
      chars  <- Gen.listOfN(length, genInvalidChar)
    } yield chars.mkString
  }

  def stringsLongerThan(minLength: Int): Gen[String] = for {
    maxLength <- (minLength * 2).max(100)
    length    <- Gen.chooseNum(minLength + 1, maxLength)
    chars     <- listOfN(length, genValidChar)
  } yield chars.mkString

  def stringsExceptSpecificValues(excluded: Seq[String]): Gen[String] =
    nonEmptyString suchThat (!excluded.contains(_))

  def oneOf[T](xs: Seq[Gen[T]]): Gen[T] =
    if (xs.isEmpty) {
      throw new IllegalArgumentException("oneOf called on empty collection")
    } else {
      val vector = xs.toVector
      choose(0, vector.size - 1).flatMap(vector(_))
    }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }
}
