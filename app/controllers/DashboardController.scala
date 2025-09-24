package controllers

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import models.{DashboardData, PensionSchemeDetails, PstrNumber}
import pages.DashboardPage
import play.api.Logging
import play.api.i18n.I18nSupport
import play.api.mvc._
import queries.PensionSchemeDetailsQuery
import queries.dashboard.TransfersOverviewQuery
import repositories.DashboardSessionRepository
import services.TransferService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import viewmodels.PaginatedAllTransfersViewModel
import views.html.DashboardView

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DashboardController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    repo: DashboardSessionRepository,
    identify: IdentifierAction,
    transferService: TransferService,
    view: DashboardView,
    appConfig: FrontendAppConfig
  )(implicit ec: ExecutionContext
  ) extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad: Action[AnyContent] = identify.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    val id                         = request.authenticatedUser.internalId

    repo.get(id).flatMap {
      case None =>
        logger.warn(s"[DashboardController][onPageLoad] No dashboard data found for $id")
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))

      case Some(dashboardData) =>
        dashboardData.get(PensionSchemeDetailsQuery).fold {
          logger.warn(s"[DashboardController][onPageLoad] Missing PensionSchemeDetails for $id")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        } { pensionSchemeDetails =>
          renderDashboard(dashboardData, pensionSchemeDetails)
        }
    }
  }

  private def renderDashboard(
      dashboardData: DashboardData,
      pensionSchemeDetails: PensionSchemeDetails
    )(implicit request: Request[_],
      hc: HeaderCarrier
    ): Future[Result] = {
    transferService.getAllTransfersData(dashboardData, pensionSchemeDetails.pstrNumber).flatMap {
      _.fold(
        err => {
          logger.warn(s"[DashboardController] getAllTransfersData failed: $err")
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
        },
        updated => {
          val items = updated.get(TransfersOverviewQuery).getOrElse(Seq.empty)
          val vm    = PaginatedAllTransfersViewModel.build(
            items      = items,
            page       = currentPage,
            pageSize   = appConfig.transfersPerPage,
            urlForPage = pageUrl
          )

          repo.set(updated).map { _ =>
            Ok(view(pensionSchemeDetails.schemeName, DashboardPage.nextPage(updated).url, vm))
          }
        }
      )
    }
  }

  private def currentPage(implicit req: Request[_]): Int =
    req.getQueryString("page").flatMap(_.toIntOption).filter(_ >= 1).getOrElse(1)

  private def pageUrl(n: Int): String =
    s"${routes.DashboardController.onPageLoad().url}?page=$n"

}
