package forms

import forms.behaviours.OptionFieldBehaviours
import models.MemberIsResidentUK
import play.api.data.FormError

class MemberIsResidentFormProviderSpec extends OptionFieldBehaviours {

  val form = new MemberIsResidentUKFormProvider()()

  ".value" - {

    val fieldName = "value"
    val requiredKey = "memberIsResidentUk.error.required"

    behave like optionsField[MemberIsResidentUK](
      form,
      fieldName,
      validValues  = MemberIsResidentUK.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
