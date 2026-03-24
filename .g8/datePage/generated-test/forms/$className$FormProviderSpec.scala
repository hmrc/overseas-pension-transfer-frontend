package forms

import java.time.{LocalDate, ZoneOffset}
import forms.behaviours.DateBehaviours
import play.api.i18n.Messages
import play.api.test.Helpers.stubMessages

class $className$FormProviderSpec extends DateBehaviours with SpecBase {

  private implicit val messages: Messages = stubMessages()
  private val form = new $className$FormProvider()()

  ".value" - {

    val validData = datesBetween(
      min = LocalDate.of(2000, 1, 1),
      max = today
    )

    behave like dateField(form, "value", validData)

    behave like mandatoryDateField(form, "value", "$className;format="decap"$.error.required.all")
  }
}
