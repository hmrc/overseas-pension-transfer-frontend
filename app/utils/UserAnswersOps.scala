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

package utils

import models.{MemberDetails, QROPSDetails, SchemeManagersDetails, SubmissionDetails, TransferDetails, UserAnswers}
import pages.QuestionPage

import scala.util.{Success, Try}

object UserAnswersOps {

  implicit class UpdatedUserAnswers(val answers: UserAnswers) extends AnyVal {

    def updateMemberDetails(f: MemberDetails => MemberDetails): UserAnswers =
      answers.set { formData =>
        val updated = answers.get(_.memberDetails).map(f)
        formData.copy(memberDetails = updated)
      }

    def updateMemberDetailsOrCreate(f: MemberDetails => MemberDetails): UserAnswers =
      answers.set { formData =>
        val updated = f(answers.get(_.memberDetails).getOrElse(MemberDetails()))
        formData.copy(memberDetails = Some(updated))
      }

    def updateTransferDetails(f: TransferDetails => TransferDetails): UserAnswers =
      answers.set { formData =>
        val updated = answers.get(_.transferDetails).map(f)
        formData.copy(transferDetails = updated)
      }
    /*
    def updateTransferDetailsOrCreate(f: TransferDetails => TransferDetails): UserAnswers =
      answers.set { formData =>
        val updated = f(answers.get(_.transferDetails).getOrElse(TransferDetails()))
        formData.copy(transferDetails = Some(updated))
      }
     */

    def updateQROPSDetails(f: QROPSDetails => QROPSDetails): UserAnswers =
      answers.set { formData =>
        val updated = answers.get(_.qropsDetails).map(f)
        formData.copy(qropsDetails = updated)
      }
    /*
    def updateQROPSDetailsOrCreate(f: QROPSDetails => QROPSDetails): UserAnswers =
      answers.set { formData =>
        val updated = f(answers.get(_.qropsDetails).getOrElse(QROPSDetails()))
        formData.copy(qropsDetails = Some(updated))
      }
     */

    def updateSchemeManagerDetails(f: SchemeManagersDetails => SchemeManagersDetails): UserAnswers =
      answers.set { formData =>
        val updated = answers.get(_.schemeManagersDetails).map(f)
        formData.copy(schemeManagersDetails = updated)
      }
    /*
    def updateSchemeManagerDetailsOrCreate(f: SchemeManagersDetails => SchemeManagersDetails): UserAnswers =
      answers.set { formData =>
        val updated = f(answers.get(_.schemeManagersDetails).getOrElse(SchemeManagersDetails()))
        formData.copy(schemeManagersDetails = Some(updated))
      }
     */

    def updateSubmissionDetails(f: SubmissionDetails => SubmissionDetails): UserAnswers =
      answers.set { formData =>
        val updated = answers.get(_.submissionDetails).map(f)
        formData.copy(submissionDetails = updated)
      }
    /*
    def updateSubmissionDetailsOrCreate(f: SubmissionDetails => SubmissionDetails): UserAnswers =
      answers.set { formData =>
        val updated = f(answers.get(_.submissionDetails).getOrElse(SubmissionDetails()))
        formData.copy(submissionDetails = Some(updated))
      }
     */

    def syncToFormData[A](
        valueOpt: Option[A]
      )(
        page: QuestionPage[A]
      )(
        update: A => UserAnswers => UserAnswers
      )(
        userAnswers: UserAnswers
      ): Try[UserAnswers] = {
      val updated = valueOpt match {
        case Some(value) =>
          val updatedAnswers = update(value)(userAnswers)
          updatedAnswers.remove(page).map(identity).getOrElse(updatedAnswers)

        case None =>
          userAnswers
      }

      Success(updated)
    }
  }
}
