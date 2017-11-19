package pipeflow.system

import java.time.LocalDateTime

import com.typesafe.config.{Config, ConfigFactory}
import pipeflow.dsl.nodes.Node
import pipeflow.system.iso8601.RepeatingInterval

import scala.util.{Failure, Success}


object PipeFlowSystem {
  type NodeBuilder = (LocalDateTime) => Node

  def apply(name: String): PipeFlowSystem = new PipeFlowSystem(name, ConfigFactory.load())
}

class PipeFlowSystem(name: String, config: Config) {

  import PipeFlowSystem.NodeBuilder

  case class Schedule(interval: RepeatingInterval, nodeBuilder: NodeBuilder)

  var schedules = List.empty[Schedule]

  def schedule(iso8601Expression: String)(nodeBuilder: NodeBuilder): Unit =
    RepeatingInterval(iso8601Expression) match {
      case Success(repeatingInterval) => schedule(repeatingInterval)(nodeBuilder)
      case Failure(exception) => throw exception
    }

  def schedule(repeatingInterval: RepeatingInterval)(nodeBuilder: NodeBuilder): Unit = {
    schedules :+= Schedule(repeatingInterval, nodeBuilder)
  }

  def run(): Unit = ???
}
