package forms

import forms.behaviours.BooleanFieldBehaviours
import play.api.data.FormError

class AdditionalQuotedShareFormProviderSpec extends BooleanFieldBehaviours {

  val requiredKey = "additionalQuotedShare.error.required"
  val invalidKey  = "error.boolean"

  val form = new AdditionalQuotedShareFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like booleanField(
      form,
      fieldName,
      invalidError = FormError(fieldName, invalidKey)
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
