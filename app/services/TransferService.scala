package services

import connectors.TransferConnector
import models.{DashboardData, PstrNumber}
import models.dtos.GetAllTransfersDTO
import models.responses.{AllTransfersUnexpectedError, NoTransfersFound, TransferError}
import queries.dashboard.{TransfersLastSyncedAtQuery, TransfersOverviewQuery}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

@Singleton
class TransferService @Inject() (
                                           connector: TransferConnector
                                         )(implicit ec: ExecutionContext) {


  def getAllTransfersData(current: DashboardData, pstr: PstrNumber)
                 (implicit hc: HeaderCarrier): Future[Either[TransferError, DashboardData]] =
    connector.getAllTransfers(pstr).map {
      case Left(NoTransfersFound) =>

        current.set(TransfersOverviewQuery, Seq.empty) match {
          case Success(updated) => Right(updated)
          case Failure(e)       => Left(AllTransfersUnexpectedError("Failed to clear transfers data", Some(e.getMessage)))
        }

      case Left(err) =>
        Left(err)
      case Right(dto) =>
        mergeFromDto(current, dto) match {
          case Success(updated) => Right(updated)
          case Failure(e)       => Left(AllTransfersUnexpectedError("Failed to merge transfers into dashboard", Some(e.getMessage)))
        }
    }

  private def mergeFromDto(current: DashboardData, dto: GetAllTransfersDTO): Try[DashboardData] =
    for {
      dd1 <- current.set(TransfersOverviewQuery, dto.transfers)
      dd2 <- dd1.set(TransfersLastSyncedAtQuery, dto.lastUpdated)
    } yield dd2
}
