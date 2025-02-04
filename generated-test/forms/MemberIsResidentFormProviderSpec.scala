package forms

import forms.behaviours.OptionFieldBehaviours
import models.MemberIsResident
import play.api.data.FormError

class MemberIsResidentFormProviderSpec extends OptionFieldBehaviours {

  val form = new MemberIsResidentFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "memberIsResident.error.required"

    behave like optionsField[MemberIsResident](
      form,
      fieldName,
      validValues  = MemberIsResident.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
