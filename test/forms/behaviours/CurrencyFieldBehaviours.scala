package forms.behaviours

import play.api.data.{Form, FormError}

trait CurrencyFieldBehaviours extends FieldBehaviours {

  def currencyField(form: Form[_], fieldName: String, nonNumericError: FormError): Unit = {

    "must not bind non-numeric numbers" in {

      forAll(nonNumerics -> "nonNumeric") {
        nonNumeric =>
          val result = form.bind(Map(fieldName -> nonNumeric)).apply(fieldName)
          result.errors mustEqual Seq(nonNumericError)
      }
    }
  }

  def currencyFieldWithMinimum(form: Form[_], fieldName: String, minimum: BigDecimal, expectedError: FormError): Unit = {

    "must not bind when the value is less than the minimum" in {

      val result = form.bind(Map(fieldName -> (minimum - 0.01).toString)).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }
  }

  def currencyFieldWithMaximum(form: Form[_], fieldName: String, maximum: BigDecimal, expectedError: FormError): Unit = {

    "must not bind when the value is greater than the maximum" in {

      val result = form.bind(Map(fieldName -> (maximum + 0.01).toString)).apply(fieldName)
      result.errors mustEqual Seq(expectedError)
    }
  }
}
