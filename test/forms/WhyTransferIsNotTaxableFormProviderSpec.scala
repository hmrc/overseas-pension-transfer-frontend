package forms

import forms.behaviours.CheckboxFieldBehaviours
import models.WhyTransferIsNotTaxable
import play.api.data.FormError

class WhyTransferIsNotTaxableFormProviderSpec extends CheckboxFieldBehaviours {

  val form = new WhyTransferIsNotTaxableFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "whyTransferIsNotTaxable.error.required"

    behave like checkboxField[WhyTransferIsNotTaxable](
      form,
      fieldName,
      validValues  = WhyTransferIsNotTaxable.values,
      invalidError = FormError(s"$fieldName[0]", "error.invalid")
    )

    behave like mandatoryCheckboxField(
      form,
      fieldName,
      requiredKey
    )
  }
}
