package forms

import forms.behaviours.OptionFieldBehaviours
import models.MemberSelectLastUkAddress
import play.api.data.FormError

class MemberSelectLastUkAddressFormProviderSpec extends OptionFieldBehaviours {

  val form = new MemberSelectLastUkAddressFormProvider()()

  ".value" - {

    val fieldName   = "value"
    val requiredKey = "memberSelectLastUkAddress.error.required"

    behave like optionsField[MemberSelectLastUkAddress](
      form,
      fieldName,
      validValues  = MemberSelectLastUkAddress.values,
      invalidError = FormError(fieldName, "error.invalid")
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
