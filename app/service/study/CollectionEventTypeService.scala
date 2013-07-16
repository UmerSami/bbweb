package service.study

import infrastructure.commands._
import infrastructure.events
import domain._
import domain.study.{
  CollectionEventAnnotationType,
  CollectionEventType,
  CollectionEventTypeAnnotationType,
  CollectionEventTypeId,
  DisabledStudy,
  SpecimenGroup,
  SpecimenGroupId,
  SpecimenGroupCollectionEventType,
  StudyAnnotationType,
  Study,
  StudyId
}
import domain.study.Study._
import service._

import org.eligosource.eventsourced.core._
import org.slf4j.LoggerFactory

import scalaz._
import Scalaz._

/**
 * This is the Collection Event Type Domain Service.
 *
 * It handles commands that deal with a Collection Event Type.
 *
 * @param studyRepository The read-only repository for study entities.
 * @param collectionEventTypeRepository The repository for specimen group entities.
 * @param specimenGroupRepository The read-only repository for specimen group entities.
 * @param sg2cetRepo The value object repository that associates a specimen group to a
 *         collection event type.
 * @param at2cetRepo The value object repository that associates a collection event annotation
 *         type to a collection event type.
 */
protected[service] class CollectionEventTypeService()
  extends CommandHandler {
  import CollectionEventTypeService._

  /**
   * This partial function handles each command. The command is contained within the
   * StudyProcessorMsg.
   *
   *  If the command is invalid, then this method throws an Error exception.
   */
  def process = {

    case msg: StudyProcessorMsg =>
      msg.cmd match {
        // collection event types
        case cmd: AddCollectionEventTypeCmd =>
          addCollectionEventType(cmd, msg.study, msg.listeners, msg.id)
        case cmd: UpdateCollectionEventTypeCmd =>
          updateCollectionEventType(cmd, msg.study, msg.listeners)
        case cmd: RemoveCollectionEventTypeCmd =>
          removeCollectionEventType(cmd, msg.study, msg.listeners)

        case _ =>
          throw new Error("invalid command received")
      }

    case _ =>
      throw new Error("invalid message received")
  }

  private def addCollectionEventType(
    cmd: AddCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[CollectionEventType] = {

    def addItem(item: CollectionEventType) {
      CollectionEventTypeRepository.updateMap(item);
      listeners sendEvent CollectionEventTypeAddedEvent(
        study.id, item.id, item.name, item.description, item.recurring)
    }

    val item = for {
      cetId <- id.toSuccess(DomainError("collection event type ID is missing"))
      ceventType <- CollectionEventTypeRepository.getValues.exists(
        item => item.studyId.equals(study.id) && !item.id.equals(id) && item.name.equals(cmd.name)).success
      validSgs <- StudyValidation.validateSpecimenGroupIds(study, cmd.specimenGroupIds)
      validAts <- StudyValidation.validateCollectionEventAnnotationTypeIds(study, cmd.annotationTypeIds)
      newItem <- study.addCollectionEventType(cmd, cetId)
      addItem <- addItem(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "addCollectionEventType", cmd, item)
    item
  }

  private def updateCollectionEventType(
    cmd: UpdateCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {
    def update(item: CollectionEventType): CollectionEventType = {
      CollectionEventTypeRepository.updateMap(item)
      listeners sendEvent CollectionEventTypeUpdatedEvent(
        study.id, item.id, item.name, item.description, item.recurring)
      item
    }

    val item = for {
      validStudy <- StudyValidation.validateCollectionEventTypeId(study, cmd.id)
      validSgs <- StudyValidation.validateSpecimenGroupIds(study, cmd.specimenGroupIds)
      validAts <- StudyValidation.validateCollectionEventAnnotationTypeIds(study, cmd.annotationTypeIds)
      prevItem <- CollectionEventTypeRepository.getByKey(new CollectionEventTypeId(cmd.id))
      newItem <- study.updateCollectionEventType(prevItem, cmd)
      item <- update(newItem).success
    } yield newItem
    CommandHandler.logMethod(log, "updateCollectionEventType", cmd, item)
    item
  }

  private def removeCollectionEventType(
    cmd: RemoveCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventType] = {

    def removeItem(item: CollectionEventType) = {
      CollectionEventTypeRepository.remove(item)
      listeners sendEvent CollectionEventTypeRemovedEvent(study.id, item.id)
    }

    val item = for {
      validStudy <- StudyValidation.validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.id)
      item <- study.removeCollectionEventType(collectionEventTypeRepository, cmd)
      removedItem <- removeItem(item).success
    } yield item
    CommandHandler.logMethod(log, "removeCollectionEventType", cmd, item)
    item
  }

  private def addSpecimenGroupToCollectionEventType(
    collectionEventType: CollectionEventType,
    specimenGroupIds: Set[String]): DomainValidation[SpecimenGroupCollectionEventType] = {

    def createItem(id: String, sg: SpecimenGroup, cet: CollectionEventType) = {
      val item = cet.addSpecimenGroup(id, sg, cmd.count, cmd.amount)
      SpecimenGroupCollectionEventTypeRepository.updateMap(item)
      listeners sendEvent SpecimenGroupAddedToCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.specimenGroupId, item.count, item.amount)
      item
    }

    val item = for {
      sg2cetId <- id.toSuccess(DomainError("sg to cet ID is missing"))
      sg <- StudyValidation.validateSpecimenGroupId(study, specimenGroupRepository, cmd.specimenGroupId)
      cet <- StudyValidation.validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      newItem <- createItem(sg2cetId, sg, cet).success
    } yield newItem
    CommandHandler.logMethod(log, "addSpecimenGroupToCollectionEventType", cmd, item)
    item
  }

  private def removeSpecimenGroupFromCollectionEventType(
    cmd: RemoveSpecimenGroupFromCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[SpecimenGroupCollectionEventType] = {

    def removeItem(item: SpecimenGroupCollectionEventType) = {
      SpecimenGroupCollectionEventTypeRepository.remove(item)
      listeners sendEvent SpecimenGroupRemovedFromCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.specimenGroupId)
      item.success
    }

    val item = for {
      item <- sg2cetRepo.getByKey(cmd.id)
      removedItem <- removeItem(item)
    } yield removedItem
    CommandHandler.logMethod(log, "removeSpecimenGroupFromCollectionEventType", cmd, item)
    item
  }

  private def addAnnotationTypeToCollectionEventType(
    cmd: AddAnnotationTypeToCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter,
    id: Option[String]): DomainValidation[CollectionEventTypeAnnotationType] = {
    def createItem(at2cetid: String, cet: CollectionEventType,
      cetAt: CollectionEventAnnotationType): CollectionEventTypeAnnotationType = {
      val item = cet.addAnnotationType(at2cetid, cetAt, cmd.required)
      cet2atRepo.updateMap(item)
      listeners sendEvent AnnotationTypeAddedToCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.annotationTypeId)
      item
    }

    val item = for {
      at2cetId <- id.toSuccess(DomainError("at to cet ID is missing"))
      v1 <- StudyValidation.validateCollectionEventTypeId(study, collectionEventTypeRepository, cmd.collectionEventTypeId)
      v2 <- StudyValidation.validateCollectionEventAnnotationTypeId(study, annotationTypeRepo, cmd.annotationTypeId)
      newItem <- createItem(at2cetId, v1, v2).success
    } yield newItem
    CommandHandler.logMethod(log, "addAnnotationTypeToCollectionEventType", cmd, item)
    item
  }

  private def removeAnnotationTypeFromCollectionEventType(
    cmd: RemoveAnnotationTypeFromCollectionEventTypeCmd,
    study: DisabledStudy,
    listeners: MessageEmitter): DomainValidation[CollectionEventTypeAnnotationType] = {

    def removeItem(item: CollectionEventTypeAnnotationType): CollectionEventTypeAnnotationType = {
      cet2atRepo.remove(item)
      listeners sendEvent AnnotationTypeRemovedFromCollectionEventTypeEvent(
        study.id, item.id, item.collectionEventTypeId, item.annotationTypeId)
      item
    }

    val item = for {
      item <- cet2atRepo.getByKey(cmd.id)
      removedItem <- removeItem(item).success
    } yield removedItem
    CommandHandler.logMethod(log, "removeAnnotationTypeFromCollectionEventType", cmd, item)
    item
  }

}

object CollectionEventTypeService {
  val log = LoggerFactory.getLogger(SpecimenGroupService.getClass)
}
