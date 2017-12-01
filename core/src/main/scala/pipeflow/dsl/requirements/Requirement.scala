package pipeflow.dsl.requirements


trait Requirement {
  // def children: Iterable[Requirement] = Iterable.empty
}

object Requirement {

  trait Evidence[T] {
    def requirement(from: T): Requirement
  }
}
