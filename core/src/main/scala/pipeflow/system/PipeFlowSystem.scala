package pipeflow.system

import java.time.{Clock, LocalDateTime}

import akka.actor.{ActorRef, ActorSystem, Props}
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.LoggerFactory
import pipeflow.dsl.tasks.TaskLike
import pipeflow.iso8601.RepeatingInterval
import pipeflow.system.scheduling.periodic.PeriodicScheduler
import pipeflow.system.scheduling.tasks.TaskScheduler
import pipeflow.system.scheduling.tasks.TaskScheduler.NodeCreated

import scala.concurrent.ExecutionContext


private[system] trait ActorMaker {

  protected def system: ActorSystem

  protected def actorOf(props: Props): ActorRef = system.actorOf(props)
  protected def actorOf(props: Props, name: String): ActorRef = system.actorOf(props, name)
}

object PipeFlowSystem {
  type ScheduleNodeBuilder = (LocalDateTime) => TaskLike

  private val logger = LoggerFactory.getLogger(PipeFlowSystem.getClass.getName.split("[.$]").last)

  private[system] object ActorNames {
    val TaskScheduler = "task-scheduler"
  }

  class ScheduleRepeatingIntervalException(msg: String) extends Exception(msg)

  def apply(name: String)(implicit clock: Clock, executionContext: ExecutionContext): PipeFlowSystem = {
    val config = ConfigFactory.load()
    val system = ActorSystem(name, config)
    new PipeFlowSystem(name, config, system)
  }
}

class PipeFlowSystem private[system] (val name: String,
                                      val config: Config,
                                      protected val system: ActorSystem)(
                                      implicit clock: Clock, executionContext: ExecutionContext) extends ActorMaker {

  import PipeFlowSystem.{ScheduleNodeBuilder, ScheduleRepeatingIntervalException, logger, ActorNames}

  private val taskScheduler = actorOf(TaskScheduler.props(), ActorNames.TaskScheduler)

  def schedule(iso8601RepeatingInterval: String)(nodeBuilder: ScheduleNodeBuilder): Unit = {
    RepeatingInterval(iso8601RepeatingInterval) match {
      case Right(repeatingInterval) => schedule(repeatingInterval)(nodeBuilder)
      case Left(msg) => throw new ScheduleRepeatingIntervalException(msg)
    }
  }

  def schedule(repeatingInterval: RepeatingInterval)(nodeBuilder: ScheduleNodeBuilder): Unit = {
    actorOf(PeriodicScheduler.props(taskScheduler, repeatingInterval, nodeBuilder))
  }

  def schedule(node: TaskLike): Unit = {
    taskScheduler ! NodeCreated(node)
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
