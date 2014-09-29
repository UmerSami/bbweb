package org.biobank.domain.user

import org.biobank.domain.{ DomainValidation, DomainError, ReadWriteRepository, ReadWriteRepositoryRefImpl }

import org.slf4j.LoggerFactory

import scalaz._
import scalaz.Scalaz._

trait UserRepositoryComponent {

  val userRepository: UserRepository

  /** A repository that stores [[User]]s. */
  trait UserRepository extends ReadWriteRepository[UserId, User] {

    def allUsers(): Set[User]

    def getRegistered(id: UserId): DomainValidation[RegisteredUser]

    def getActive(id: UserId): DomainValidation[ActiveUser]

    def getLocked(id: UserId): DomainValidation[LockedUser]

    def getByEmail(email: String): DomainValidation[User]

  }
}

trait UserRepositoryComponentImpl extends UserRepositoryComponent {

  val userRepository: UserRepository = new UserRepositoryImpl

  /** An implementation of repository that stores [[User]]s.
    *
    * This repository uses the [[ReadWriteRepository]] implementation.
    */
  class UserRepositoryImpl
      extends ReadWriteRepositoryRefImpl[UserId, User](v => v.id)
      with UserRepository {

    def nextIdentity: UserId = new UserId(nextIdentityAsString)

    def allUsers(): Set[User] = getValues.toSet

    def getRegistered(id: UserId) = getByKey(id).map(_.asInstanceOf[RegisteredUser])

    def getActive(id: UserId) = getByKey(id).map(_.asInstanceOf[ActiveUser])

    def getLocked(id: UserId) = getByKey(id).map(_.asInstanceOf[LockedUser])

    def getByEmail(email: String): DomainValidation[User] = {
      getValues.find(_.email == email) match {
        case Some(user) => user.success
        case None => DomainError(s"user with email not found: $email").failNel
      }
    }
  }
}