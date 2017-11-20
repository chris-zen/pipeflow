package pipeflow.system

import java.time.LocalDateTime

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import pipeflow.dsl.nodes.Node
import pipeflow.system.iso8601.RepeatingInterval

import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success}


object PipeFlowSystem {
  type NodeBuilder = (LocalDateTime) => Node

  private val logger = LoggerFactory.getLogger(PipeFlowSystem.getClass.getName.split("[.$]").last)

  def apply(name: String): PipeFlowSystem = new PipeFlowSystem(name, ConfigFactory.load())
}

class PipeFlowSystem(val name: String, val config: Config) {

  import PipeFlowSystem.{logger, NodeBuilder}

  case class Schedule(interval: RepeatingInterval, nodeBuilder: NodeBuilder)

  val system = ActorSystem(name, config)

  def schedule(iso8601Expression: String)(nodeBuilder: NodeBuilder): Unit = {
    RepeatingInterval(iso8601Expression) match {
      case Success(repeatingInterval) => schedule(repeatingInterval)(nodeBuilder)
      case Failure(exception) => throw exception
    }
  }

  def schedule(repeatingInterval: RepeatingInterval)(nodeBuilder: NodeBuilder): Unit = {
    // TODO Create an scheduling actor
  }

  def run(node: Node): Unit = {
    // TODO Materialize the node graph into actors
  }

  def awaitTermination(): Unit = {
    logger.info("Awaiting PipeFlow termination ...")
    system.awaitTermination()
  }

  def shutdown(): Unit = {
    logger.info("Shutting down PipeFlow ...")
    system.shutdown()
  }
}
