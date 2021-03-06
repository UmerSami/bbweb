package org.biobank.service.users

import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import com.google.inject.ImplementedBy
import javax.inject._
import org.biobank.ValidationKey
import org.biobank.domain._
import org.biobank.domain.user._
import org.biobank.dto._
import org.biobank.infrastructure._
import org.biobank.infrastructure.command.UserCommands._
import org.biobank.infrastructure.event.UserEvents._
import org.biobank.service._
import org.slf4j.LoggerFactory
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future
import scala.concurrent.duration._

import scalaz.Scalaz._
import scalaz.Validation.FlatMap._

@ImplementedBy(classOf[UsersServiceImpl])
trait UsersService {

  def getAll: Set[User]

  def getCountsByStatus(): UserCountsByStatus

  def getUsers[T <: User](nameFilter:  String,
                          emailFilter: String,
                          status:      String,
                          sortFunc:    (User, User) => Boolean,
                          order:       SortOrder)
      : DomainValidation[Seq[User]]

  def getUser(id: String): DomainValidation[User]

  def getByEmail(email: String): DomainValidation[User]

  def validatePassword(email: String, enteredPwd: String): DomainValidation[User]

  def processCommand(cmd: UserCommand): Future[DomainValidation[User]]

}

class UsersServiceImpl @javax.inject.Inject() (
  @Named("usersProcessor") val processor: ActorRef,
  val userRepository: UserRepository,
  val passwordHasher: PasswordHasher)
    extends UsersService {
  import org.biobank.CommonValidations._

  case object InvalidPassword extends ValidationKey

  val Log = LoggerFactory.getLogger(this.getClass)

  implicit val timeout: Timeout = 5.seconds

  def getAll: Set[User] = {
    userRepository.allUsers
  }

  def getCountsByStatus(): UserCountsByStatus = {
    // FIXME should be replaced by DTO query to the database
    val users = userRepository.getValues
      UserCountsByStatus(
        total           = users.size,
        registeredCount = users.collect { case u: RegisteredUser => u }.size,
        activeCount     = users.collect { case u: ActiveUser     => u }.size,
        lockedCount     = users.collect { case u: LockedUser     => u }.size
      )
  }

  def getUsers[T <: User](nameFilter:  String,
                          emailFilter: String,
                          status:      String,
                          sortFunc:    (User, User) => Boolean,
                          order:       SortOrder)
      : DomainValidation[Seq[User]] = {
    val allUsers = userRepository.getValues

    val usersFilteredByName = if (!nameFilter.isEmpty) {
      val nameFilterLowerCase = nameFilter.toLowerCase
      allUsers.filter { _.name.toLowerCase.contains(nameFilterLowerCase) }
    } else {
      allUsers
    }

    val usersFilteredByEmail = if (!emailFilter.isEmpty) {
      val emailFilterLowerCase = emailFilter.toLowerCase
      usersFilteredByName.filter { _.email.toLowerCase.contains(emailFilterLowerCase) }
    } else {
      usersFilteredByName
    }

    val usersFilteredByStatus = status match {
      case "all" =>
        usersFilteredByEmail.success
      case "RegisteredUser" =>
        usersFilteredByEmail.collect { case u: RegisteredUser => u }.success
      case "ActiveUser" =>
        usersFilteredByEmail.collect { case u: ActiveUser => u }.success
      case "LockedUser" =>
        usersFilteredByEmail.collect { case u: LockedUser => u }.success
      case _ =>
        InvalidStatus(status).failureNel
    }

    usersFilteredByStatus.map { users =>
      val result = users.toSeq.sortWith(sortFunc)

      if (order == AscendingOrder) {
        result
      } else {
        result.reverse
      }
    }
  }

  def getUser(id: String): DomainValidation[User] = {
    userRepository.getByKey(UserId(id))
      .leftMap(_ => IdNotFound(s"user with id does not exist: $id").nel)
  }

  def getByEmail(email: String): DomainValidation[User] = {
    userRepository.getByEmail(email)
  }

  def validatePassword(email: String, enteredPwd: String): DomainValidation[User] = {
    for {
      user <- userRepository.getByEmail(email)
      validPwd <- {
        if (passwordHasher.valid(user.password, user.salt, enteredPwd)) {
          user.success
        } else {
          InvalidPassword.failureNel
        }
      }
      notLocked <- UserHelper.isUserNotLocked(user)
    } yield user
  }

  def processCommand(cmd: UserCommand): Future[DomainValidation[User]] =
    ask(processor, cmd).mapTo[DomainValidation[UserEvent]].map { validation =>
      for {
        event <- validation
        user  <- userRepository.getByKey(UserId(event.id))
      } yield user
    }

}
