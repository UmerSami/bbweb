package org.biobank.domain.centre

import com.google.inject.ImplementedBy
import javax.inject.Singleton
import org.biobank.domain._
import scalaz.Scalaz._
import scalaz._

@ImplementedBy(classOf[CentreRepositoryImpl])
trait CentreRepository extends ReadWriteRepository[CentreId, Centre] {

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre]

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre]

  def getByLocationId(uniqueId: String): DomainValidation[Centre]

}

@Singleton
class CentreRepositoryImpl
    extends ReadWriteRepositoryRefImpl[CentreId, Centre](v => v.id)
    with CentreRepository {
  import org.biobank.CommonValidations._

  def nextIdentity: CentreId = new CentreId(nextIdentityAsString)

  def notFound(id: CentreId) = IdNotFound(s"centre id: $id")

  override def getByKey(id: CentreId): DomainValidation[Centre] = {
    getMap.get(id).toSuccessNel(notFound(id).toString)
  }

  def getDisabled(id: CentreId): DomainValidation[DisabledCentre] = {
    getByKey(id) match {
      case Success(s: DisabledCentre) => s.success
      case Success(s) => InvalidStatus(s"centre is not disabled: $id").failureNel
      case Failure(err) => err.failure[DisabledCentre]
    }
  }

  def getEnabled(id: CentreId): DomainValidation[EnabledCentre] = {
    getByKey(id) match {
      case Success(s: EnabledCentre) => s.success
      case Success(s) => InvalidStatus(s"centre is not enabled: $id").failureNel
      case Failure(err) => err.failure[EnabledCentre]
    }
  }

  def getByLocationId(uniqueId: String): DomainValidation[Centre] = {
    val centres = getValues.filter { c => !c.locations.filter( l => l.uniqueId == uniqueId ).isEmpty}
    if (centres.isEmpty) {
      EntityCriteriaError(s"centre with location id does not exist: $uniqueId").failureNel
    } else if (centres.size > 1){
      EntityCriteriaError(s"multiple centres with location id: $uniqueId").failureNel
    } else {
      centres.head.success
    }
  }
}
