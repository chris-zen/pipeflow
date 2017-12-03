package pipeflow.dsl.actions


trait Action {

}

object Action {

  trait Evidence[T] {
    def action(from: T): Action
  }
}
