package pipeflow.system.scheduling.tasks

import akka.actor.Props
import pipeflow.dsl.nodes.Node

object TaskScheduler {

  case class NodeCreated(node: Node) extends AnyVal

  // TODO Add task scheduler support
  def props(): Props = Props.empty
}
