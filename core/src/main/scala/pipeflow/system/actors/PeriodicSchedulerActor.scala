package pipeflow.system.actors

import java.time.{Clock, OffsetDateTime}
import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, PoisonPill, Props}
import akka.event.LoggingReceive
import akka.pattern.pipe
import pipeflow.dsl.nodes.Node
import pipeflow.system.PipeFlowSystem.NodeBuilder
import pipeflow.system.iso8601.{IntervalDuration, RepeatingInterval}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

object PeriodicSchedulerActor {

  case class Tick(recurrences: Option[Long], scheduledDateTime: OffsetDateTime)

  case class NodeCreated(node: Node)

  def props(taskSchedulerRef: ActorRef, repeatingInterval: RepeatingInterval, nodeBuilder: NodeBuilder)(
            implicit clock: Clock, executionContext: ExecutionContext): Props =
    Props(new PeriodicSchedulerActor(taskSchedulerRef, repeatingInterval, nodeBuilder))
}

class PeriodicSchedulerActor(taskSchedulerRef: ActorRef,
                             repeatingInterval: RepeatingInterval,
                             nodeBuilder: NodeBuilder)(
                             implicit clock: Clock,
                             executionContext: ExecutionContext) extends Actor {

  import PeriodicSchedulerActor.{NodeCreated, Tick}

  override def preStart(): Unit = {
    if (repeatingInterval.recurrences.forall(_ > 0)) {
      val now = OffsetDateTime.now(clock)
      val start = repeatingInterval.start.getOrElse(now)
      val nextDateTime = calculateNextDateTime(now, start, repeatingInterval.duration)
      scheduleTick(repeatingInterval.recurrences, nextDateTime, nextTickDuration(now, nextDateTime))
    }
    else {
      self ! PoisonPill
    }
  }

  def receive = LoggingReceive {
    case Tick(recurrences, scheduledDateTime) =>
      Future {
        val node = nodeBuilder(scheduledDateTime.toLocalDateTime)
        NodeCreated(node)
      }.pipeTo(taskSchedulerRef)

      val now = OffsetDateTime.now(clock)
      val nextRecurrences = recurrences.map(_ - 1)
      if (nextRecurrences.forall(_ > 0)) {
        val nextDateTime = addIntervalDuration(scheduledDateTime, repeatingInterval.duration)
        scheduleTick(nextRecurrences, nextDateTime, nextTickDuration(now, nextDateTime))
      }
      else {
        self ! PoisonPill
      }
  }

  protected def scheduleTick(recurrences: Option[Long],
                             nextDateTime: OffsetDateTime,
                             nextTickDuration: FiniteDuration) = {

    context.system.scheduler.scheduleOnce(nextTickDuration, self, Tick(recurrences, nextDateTime))
  }

  private def calculateNextDateTime(now: OffsetDateTime, start: OffsetDateTime, duration: IntervalDuration) = {
    // TODO Optimisation possible in case there are no Year, Month components in the interval duration
    // The reason for having this loop is that the interval duration could contain date components that depend on the calendar
    var nextDateTime = start
    while (nextDateTime.isBefore(now))
      nextDateTime = addIntervalDuration(nextDateTime, duration)
    nextDateTime
  }

  private def addIntervalDuration(start: OffsetDateTime, duration: IntervalDuration): OffsetDateTime = {
    start
      .plus(duration.dateDuration)
      .plus(duration.timeDuration)
  }

  private def nextTickDuration(now: OffsetDateTime, nextDateTime: OffsetDateTime): FiniteDuration =
    FiniteDuration(nextDateTime.toEpochSecond - now.toEpochSecond, TimeUnit.SECONDS)
}
