package org.biobank.service.centres

import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.CentreCommands._
import org.biobank.infrastructure.event.CentreEvents._
import org.biobank.domain.{ DomainValidation, DomainError }
import org.biobank.domain.user.UserId
import org.biobank.domain.study.{ Study, StudyId, StudyRepository }
import org.biobank.domain.centre._

import akka.actor._
import akka.pattern.ask
import scala.concurrent._
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import scala.concurrent.ExecutionContext.Implicits.global
import akka.util.Timeout
import javax.inject.{Inject => javaxInject, Named}
import com.google.inject.ImplementedBy

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[CentresServiceImpl])
trait CentresService {

  def getAll: Set[Centre]

  def getCentres[T <: Centre]
    (filter: String, status: String, sortFunc: (Centre, Centre) => Boolean, order: SortOrder)
      : DomainValidation[Seq[Centre]]

  def getCountsByStatus(): CentreCountsByStatus

  def getCentre(id: String): DomainValidation[Centre]

  def processCommand(cmd: CentreCommand): Future[DomainValidation[Centre]]
}

/**
 * This is the Centre Aggregate Application Service.
 *
 * Handles the commands to configure centres. the commands are forwarded to the Centre Aggregate
 * Processor.
 *
 * @param centreProcessor
 *
 */
class CentresServiceImpl @javaxInject() (@Named("centresProcessor") val processor: ActorRef,
                                         val centreRepository:          CentreRepository,
                                         val studyRepository:           StudyRepository)
    extends CentresService {

  val log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  /**
   * FIXME: use paging and sorting
   */
  def getAll: Set[Centre] = {
    centreRepository.getValues.toSet
  }

  def getCountsByStatus(): CentreCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val centres = centreRepository.getValues
    CentreCountsByStatus(
      total         = centres.size,
      disabledCount = centres.collect { case s: DisabledCentre => s }.size,
      enabledCount  = centres.collect { case s: EnabledCentre => s }.size
    )
  }

  def getCentres[T <: Centre](filter: String,
                              status: String,
                              sortFunc: (Centre, Centre) => Boolean,
                              order: SortOrder)
      : DomainValidation[Seq[Centre]] =  {
    val allCentres = centreRepository.getValues

    val centresFilteredByName = if (!filter.isEmpty) {
      val filterLowerCase = filter.toLowerCase
      allCentres.filter { centre => centre.name.toLowerCase.contains(filterLowerCase) }
    } else {
      allCentres
    }

    val centresFilteredByStatus = status match {
      case "all" =>
        centresFilteredByName.success
      case "DisabledCentre" =>
        centresFilteredByName.collect { case s: DisabledCentre => s }.success
      case "EnabledCentre" =>
        centresFilteredByName.collect { case s: EnabledCentre => s }.success
      case _ =>
        DomainError(s"invalid centre status: $status").failureNel
    }

    centresFilteredByStatus.map { centres =>
      val result = centres.toSeq.sortWith(sortFunc)

      if (order == AscendingOrder) {
        result
      } else {
        result.reverse
      }
    }
  }

  def getCentre(id: String): DomainValidation[Centre] = {
    centreRepository.getByKey(CentreId(id)).fold(
      err => DomainError(s"invalid centre id: $id").failureNel,
      centre => centre.success
    )
  }

  def processCommand(cmd: CentreCommand): Future[DomainValidation[Centre]] =
    ask(processor, cmd).mapTo[DomainValidation[CentreEvent]].map { validation =>
      for {
        event <- validation
        centre <- centreRepository.getByKey(CentreId(event.id))
      } yield centre
    }

}
