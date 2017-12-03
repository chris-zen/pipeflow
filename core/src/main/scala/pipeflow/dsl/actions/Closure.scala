package pipeflow.dsl.actions

import pipeflow.dsl.actions.Action.Evidence
import pipeflow.dsl.actions.Closure.ClosureBlock

object Closure {
  type ClosureBlock = () => Unit

  implicit object ClosureEvidence extends Evidence[ClosureBlock] {
    def action(block: ClosureBlock): Action = Closure(block)
  }
}

case class Closure(block: ClosureBlock) extends Action