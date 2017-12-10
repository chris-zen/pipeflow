package pipeflow.system.scheduling.tasks

import akka.actor.Props
import pipeflow.dsl.tasks.TaskLike

object TaskScheduler {

  case class NodeCreated(node: TaskLike) extends AnyVal

  // TODO Add task scheduler support
  def props(): Props = Props.empty
}
