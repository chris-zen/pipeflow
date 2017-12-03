package pipeflow.system.actors

import java.time._

import akka.actor.{ActorSystem, Cancellable}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{DurationDouble, FiniteDuration}
import org.mockito.Mockito.{verify, verifyNoMoreInteractions}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import pipeflow.dsl.nodes.{Node, Task}
import pipeflow.system.actors.PeriodicSchedulerActor.{NodeCreated, Tick}
import pipeflow.system.iso8601.{IntervalDuration, RepeatingInterval}


class PeriodicSchedulerActorSpec extends TestKit(ActorSystem("PeriodicSchedulerActorSpec"))
  with WordSpecLike with Matchers with BeforeAndAfterAll with PeriodicSchedulerActorFixtures {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "An Schedule Actor during preStart" must {
    "terminate itself for all recurrences less or equal than 0" in {
      val repeatingInterval = RepeatingInterval(
        recurrences = Some(0),
        duration = IntervalDuration(minutes = 5))

      implicit val clock: Clock = fixedClock("2017-08-15T10:00Z")

      withScheduleActor(repeatingInterval) { case (hook, _, actor) =>
        val actorProbe = TestProbe()
        actorProbe.watch(actor)
        verifyNoMoreInteractions(hook)
        actorProbe.expectTerminated(actor)
      }
    }

    "schedule the first Tick" in {
      val repeatingInterval = RepeatingInterval(duration = IntervalDuration(minutes = 5))

      implicit val clock: Clock = fixedClock("2017-08-15T10:00Z")

      val expectedNextDataTime = OffsetDateTime.parse("2017-08-15T10:00Z")
      val expectedNextTickDuration = 0.seconds

      withScheduleActor(repeatingInterval) { case (hook, _, _) =>
        verify(hook).scheduleTick(None, expectedNextDataTime, expectedNextTickDuration)
      }
    }

    "account for the recurrences" in {
      val repeatingInterval = RepeatingInterval(
        recurrences = Some(2),
        duration = IntervalDuration(minutes = 5))

      implicit val clock: Clock = fixedClock("2017-08-15T10:00Z")

      val expectedNextDataTime = OffsetDateTime.parse("2017-08-15T10:00Z")
      val expectedNextTickDuration = 0.seconds

      withScheduleActor(repeatingInterval) { case (hook, _, _) =>
        verify(hook).scheduleTick(Some(2), expectedNextDataTime, expectedNextTickDuration)
      }
    }

    "account for a future start date/time" in {
      val repeatingInterval = RepeatingInterval(
        start = Some(OffsetDateTime.parse("2017-08-15T11:00Z")),
        duration = IntervalDuration(minutes = 5))

      implicit val clock: Clock = fixedClock("2017-08-15T10:00Z")

      val expectedNextDataTime = OffsetDateTime.parse("2017-08-15T11:00Z")
      val expectedNextTickDuration = 1.hour

      withScheduleActor(repeatingInterval) { case (hook, _, _) =>
        verify(hook).scheduleTick(None, expectedNextDataTime, expectedNextTickDuration)
      }
    }

    "account for a past start date/time" in {
      val repeatingInterval = RepeatingInterval(
        start = Some(OffsetDateTime.parse("2017-08-15T09:50Z")),
        duration = IntervalDuration(minutes = 6))

      implicit val clock: Clock = fixedClock("2017-08-15T10:00Z")

      val expectedNextDataTime = OffsetDateTime.parse("2017-08-15T10:02Z")
      val expectedNextTickDuration = 2.minutes

      withScheduleActor(repeatingInterval) { case (hook, _, _) =>
        verify(hook).scheduleTick(None, expectedNextDataTime, expectedNextTickDuration)
      }
    }
  }

  "An Schedule Actor during a Tick" must {
    "account for the recurrences" in {
      val repeatingInterval = RepeatingInterval(
        recurrences = Some(2),
        duration = IntervalDuration(minutes = 5))

      implicit val clock: TimeMachine = TimeMachine("2017-08-15T10:00Z")

      withScheduleActor(repeatingInterval) { case (hook, taskSchedulerRef, actor) =>
        val actorProbe = TestProbe()
        actorProbe.watch(actor)

        var nextDateTime = OffsetDateTime.parse("2017-08-15T10:00Z")
        verify(hook).scheduleTick(Some(2), nextDateTime, 0.seconds)
        actor ! Tick(Some(2), nextDateTime)

        nextDateTime = OffsetDateTime.parse("2017-08-15T10:05Z")
        taskSchedulerRef.expectMsgPF(1.second) {
          case NodeCreated(node) =>
            verify(hook).scheduleTick(Some(1), nextDateTime, 5.minutes)
            clock.update(nextDateTime.toInstant)
        }
        actor ! Tick(Some(1), nextDateTime)

        nextDateTime = OffsetDateTime.parse("2017-08-15T10:10Z")
        taskSchedulerRef.expectMsgPF(1.second) {
          case NodeCreated(node) =>
            verifyNoMoreInteractions(hook)
            actorProbe.expectTerminated(actor)
        }
      }
    }
  }
}

trait PeriodicSchedulerActorFixtures extends MockitoSugar {

  trait Hook {
    def scheduleTick(recurrences: Option[Long],
                     nextDateTime: OffsetDateTime,
                     nextTickDuration: FiniteDuration): Cancellable
  }

  def withScheduleActor(repeatingInterval: RepeatingInterval)(
                        testBlock: (Hook, TestProbe, TestActorRef[PeriodicSchedulerActor]) => Any)(
                        implicit clock: Clock, actorSystem: ActorSystem) = {

    import ExecutionContext.Implicits.global

    def nodeBuilder(dateTime: LocalDateTime): Node = { Task("task") }

    val hook = mock[Hook]

    val taskSchedulerProbe = TestProbe()

    val actor = TestActorRef(new PeriodicSchedulerActor(taskSchedulerProbe.ref, repeatingInterval, nodeBuilder) {
      override protected def scheduleTick(recurrences: Option[Long],
                                          nextDateTime: OffsetDateTime,
                                          minimumTickDuration: FiniteDuration): Cancellable =
        hook.scheduleTick(recurrences, nextDateTime, minimumTickDuration)
    })

    testBlock(hook, taskSchedulerProbe, actor)
  }

  def fixedClock(text: String): Clock = {
    val dateTime = OffsetDateTime.parse(text)
    Clock.fixed(dateTime.toInstant, dateTime.getOffset)
  }

  object TimeMachine {
    def apply(text: String): TimeMachine = {
      new TimeMachine(OffsetDateTime.parse(text).toInstant)
    }
  }

  class TimeMachine(initialInstant: Instant) extends Clock {

    private var currentInstant = initialInstant

    def getZone: ZoneId = ZoneOffset.UTC
    def withZone(zone: ZoneId): Clock = new TimeMachine(initialInstant)
    def instant(): Instant = currentInstant

    def update(instant: Instant): this.type = {
      currentInstant = instant
      this
    }

    def update(text: String): this.type = {
      currentInstant = OffsetDateTime.parse(text).toInstant
      this
    }
  }
}
