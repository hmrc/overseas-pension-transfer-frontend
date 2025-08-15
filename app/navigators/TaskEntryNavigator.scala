package navigators

import com.google.inject.{Inject, Singleton}
import models.taskList.TaskStatus
import models.{Mode, NormalMode, TaskCategory, UserAnswers}
import play.api.mvc.Call
import queries.TaskStatusQuery

final case class TaskJourney(start: Mode => Call, cya: () => Call)

@Singleton
class TaskEntryNavigator @Inject() {

  private val journeys: Map[TaskCategory, TaskJourney] = Map(
    TaskCategory.MemberDetails        -> TaskJourney(
      mode => controllers.memberDetails.routes.MemberNameController.onPageLoad(mode),
      () => controllers.memberDetails.routes.MemberDetailsCYAController.onPageLoad()
    ),
    TaskCategory.QROPSDetails         -> TaskJourney(
      mode => controllers.qropsDetails.routes.QROPSNameController.onPageLoad(mode),
      () => controllers.qropsDetails.routes.QROPSDetailsCYAController.onPageLoad()
    ),
    TaskCategory.SchemeManagerDetails -> TaskJourney(
      mode => controllers.qropsSchemeManagerDetails.routes.SchemeManagersNameController.onPageLoad(mode),
      () => controllers.qropsSchemeManagerDetails.routes.SchemeManagerDetailsCYAController.onPageLoad()
    ),
    TaskCategory.TransferDetails      -> TaskJourney(
      mode => controllers.transferDetails.routes.OverseasTransferAllowanceController.onPageLoad(mode),
      () => controllers.transferDetails.routes.TransferDetailsCYAController.onPageLoad()
    ),
    TaskCategory.SubmissionDetails    -> TaskJourney(
      _ => controllers.routes.IndexController.onPageLoad(),
      () => controllers.routes.IndexController.onPageLoad()
    )
  )

  def entryFor(category: TaskCategory, ua: UserAnswers): Call = {
    val j = journeys(category)
    ua.get(TaskStatusQuery(category)) match {
      case Some(TaskStatus.Completed) => j.cya()
      case _                          => j.start(NormalMode)
    }
  }
}
