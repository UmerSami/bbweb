import scalaz.Validation

package object infrastructure {
  type DomainValidation[A] = Validation[DomainError, A]
  type DomainError = List[String]

  object DomainError {
    def apply(msg: String): DomainError = List(msg)
  }
}