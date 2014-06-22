package service.study

import service._
import service.commands._
import service.events._

import domain.{
  AnnotationTypeId,
  ConcurrencySafeEntity,
  DomainValidation,
  DomainError,
  Entity,
  RepositoryComponent,
  UserId
}
import domain.AnatomicalSourceType._
import domain.PreservationType._
import domain.PreservationTemperatureType._
import domain.SpecimenType._
import domain.AnnotationValueType._

import domain.study._
import Study._

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import akka.event.Logging
import akka.actor.ActorLogging
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.stm.Ref
import scala.language.postfixOps
import org.eligosource.eventsourced.core._

import scalaz._
import scalaz.Scalaz._

case class StudyMessage(cmd: Any, userId: UserId, time: Long, listeners: MessageEmitter)

trait StudyProcessorComponent {

  trait StudyProcessor extends Processor

}

/**
 * This is the Study Aggregate Processor.
 *
 * It handles the commands to configure studies.
 *
 * @param studyRepository The repository for study entities.
 * @param specimenGroupRepository The repository for specimen group entities.
 * @param cetRepo The repository for Container Event Type entities.
 * @param annotationTypeRepo The repository for Collection Event Annotation Type entities.
 * @param sg2cetRepo The value object repository that associates a specimen group to a
 *         collection event type.
 * @param at2cetRepo The value object repository that associates a collection event annotation
 *         type to a collection event type.
 */
trait StudyProcessorComponentImpl extends StudyProcessorComponent {
  self: ProcessorComponentImpl with RepositoryComponent =>

  class StudyProcessorImpl extends StudyProcessor {
    self: Emitter =>

    def receive = {
      case serviceMsg: ServiceMsg =>
        serviceMsg.cmd match {
          case cmd: AddStudyCmd =>
            process(addStudy(cmd, emitter("listeners"), serviceMsg.id))

          case cmd: UpdateStudyCmd =>
            process(updateStudy(cmd, emitter("listeners")))

          case cmd: EnableStudyCmd =>
            process(enableStudy(cmd, emitter("listeners")))

          case cmd: DisableStudyCmd =>
            process(disableStudy(cmd, emitter("listeners")))

          case cmd: SpecimenGroupCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenGroupService.process)

          case cmd: CollectionEventTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, collectionEventTypeService.process)

          case cmd: CollectionEventAnnotationTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, ceventAnnotationTypeService.process)

          case cmd: ParticipantAnnotationTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, participantAnnotationTypeService.process)

          case cmd: SpecimenLinkAnnotationTypeCommand =>
            processEntityMsg(cmd, cmd.studyId, serviceMsg.id, specimenLinkAnnotationTypeService.process)

          case other => // must be for another command handler
        }

      case _ =>
        throw new Error("invalid message received: ")
    }

    private def validateStudy(studyId: StudyId): DomainValidation[DisabledStudy] =
      studyRepository.studyWithId(studyId) match {
        case Failure(msglist) => noSuchStudy(studyId).fail
        case Success(study) => study match {
          case _: EnabledStudy => notDisabledError(study.name).fail
          case dstudy: DisabledStudy => dstudy.success
        }
      }

    private def processEntityMsg[T](
      cmd: StudyCommand,
      studyId: String,
      id: Option[String],
      processFunc: StudyProcessorMsg => DomainValidation[T]) = {
      val updatedItem = for {
        study <- validateStudy(new StudyId(studyId))
        item <- processFunc(StudyProcessorMsg(cmd, study, emitter("listeners"), id))
      } yield item
      process(updatedItem)
    }

    override protected def process[T](validation: DomainValidation[T]) = {
      validation match {
        case Success(domainObject) =>
        // update the addedBy and updatedBy fields on the study aggregate
        case Failure(x) =>
        // do nothing
      }
      super.process(validation)
    }

    private def addStudy(
      cmd: AddStudyCmd,
      listeners: MessageEmitter,
      id: Option[String]): DomainValidation[DisabledStudy] = {

      val item = for {
        studyId <- id.toSuccess(DomainError("study ID is missing"))
        newItem <- studyRepository.add(
          DisabledStudy(new StudyId(studyId), version = 0L, cmd.name, cmd.description))
        event <- listeners.sendEvent(StudyAddedEvent(
          newItem.id, newItem.name, newItem.description)).success
      } yield newItem

      logMethod("addStudy", cmd, item)
      item
    }

    private def updateStudy(
      cmd: UpdateStudyCmd,
      listeners: MessageEmitter): DomainValidation[DisabledStudy] = {

      val item = for {
        newItem <- studyRepository.update(DisabledStudy(
          new StudyId(cmd.id), cmd.expectedVersion.getOrElse(-1), cmd.name, cmd.description))
        event <- listeners.sendEvent(StudyUpdatedEvent(
          newItem.id, newItem.name, newItem.description)).success
      } yield newItem

      logMethod("updateStudy", cmd, item)
      item
    }

    private def enableStudy(
      cmd: EnableStudyCmd,
      listeners: MessageEmitter): DomainValidation[EnabledStudy] = {
      val studyId = StudyId(cmd.id)
      val item = studyRepository.enable(studyId,
        specimenGroupRepository.allSpecimenGroupsForStudy(studyId).size,
        collectionEventTypeRepository.allCollectionEventTypesForStudy(studyId).size)
      logMethod("enableStudy", cmd, item)
      item
    }

    private def disableStudy(
      cmd: DisableStudyCmd,
      listeners: MessageEmitter): DomainValidation[DisabledStudy] = {
      val studyId = StudyId(cmd.id)
      val item = studyRepository.disable(studyId)
      logMethod("enableStudy", cmd, item)
      item
    }
  }
}