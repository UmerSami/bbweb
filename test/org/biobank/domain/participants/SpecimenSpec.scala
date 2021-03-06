package org.biobank.domain.participants

import org.biobank.fixture.NameGenerator
import org.biobank.domain._
import org.biobank.domain.containers.{ ContainerId, ContainerSchemaPositionId }

import org.slf4j.LoggerFactory
import org.joda.time.DateTime

class SpecimenSpec extends DomainSpec {
  import org.biobank.TestUtils._

  val log = LoggerFactory.getLogger(this.getClass)

  val nameGenerator = new NameGenerator(this.getClass)

  "A usable specimen" can {

    "be created" when {

      "valid arguments are used" in {
        val specimen = factory.createUsableSpecimen

        val v = UsableSpecimen.create(
                id               = specimen.id,
                inventoryId      = specimen.inventoryId,
                specimenSpecId   = specimen.specimenSpecId,
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = specimen.originLocationId,
                locationId       = specimen.locationId,
                containerId      = specimen.containerId,
                positionId       = specimen.positionId,
                amount           = specimen.amount)

        v mustSucceed { spc =>
          spc must have (
            'id              (specimen.id),
            'inventoryId     (specimen.inventoryId),
            'specimenSpecId  (specimen.specimenSpecId),
            'version         (0),
            'originLocationId(specimen.originLocationId),
            'locationId      (specimen.locationId),
            'containerId     (specimen.containerId),
            'positionId      (specimen.positionId),
            'amount          (specimen.amount)
          )

          checkTimeStamps(specimen, spc.timeAdded, spc.timeModified)
          checkTimeStamps(specimen.timeCreated, spc.timeCreated)
        }
      }

    }

    "not be created" when {

      "an empty id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(""),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "IdRequired"
      }

      "an empty inventory id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = "",
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "InventoryIdInvalid"
      }

      "an empty specimen spec id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = "",
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "SpecimenSpecIdInvalid"
      }

      "an invalid version number is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = -2,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "InvalidVersion"
      }

      "an empty origin location id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = "",
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "OriginLocationIdInvalid"
      }

      "an empty location id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = "",
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "LocationIdInvalid"
      }

      "an empty container id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = Some(ContainerId("")),
                positionId       = None,
                amount           = BigDecimal(1.01))
        v mustFail "ContainerIdInvalid"
      }

      "an empty position id is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = Some(ContainerSchemaPositionId("")),
                amount           = BigDecimal(1.01))
        v mustFail "PositionInvalid"
      }

      "an negative amount is used" in {
        val v = UsableSpecimen.create(
                id               = SpecimenId(nameGenerator.next[SpecimenId]),
                inventoryId      = nameGenerator.next[Specimen],
                specimenSpecId   = nameGenerator.next[SpecimenSpec],
                version          = 0,
                timeCreated      = DateTime.now,
                originLocationId = nameGenerator.next[Location],
                locationId       = nameGenerator.next[Location],
                containerId      = None,
                positionId       = None,
                amount           = BigDecimal(-1))
        v mustFail "AmountInvalid"
      }

    }

  }
}
