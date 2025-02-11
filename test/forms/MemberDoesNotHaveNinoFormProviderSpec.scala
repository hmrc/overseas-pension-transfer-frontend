package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class MemberDoesNotHaveNinoFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "memberDoesNotHaveNino.error.required"
  val lengthKey = "memberDoesNotHaveNino.error.length"
  val maxLength = 160

  val form = new MemberDoesNotHaveNinoFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
