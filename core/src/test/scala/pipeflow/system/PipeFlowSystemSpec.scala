package pipeflow.system

import java.time.Clock

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{FlatSpecLike, Matchers}
import pipeflow.dsl.nodes.Task
import pipeflow.system.PipeFlowSystem.ScheduleRepeatingIntervalException
import pipeflow.system.scheduling.tasks.TaskScheduler.NodeCreated

import scala.collection.mutable

class PipeFlowSystemSpec extends TestKit(ActorSystem("PipeFlowSystemSpec"))
  with FlatSpecLike with Matchers with PipeFlowSystemFixtures {

  "A PipeFlowSystem" should "create a task scheduler on initialisation" in {
    withPipeFlowSystem { pipeflowSystem =>
      pipeflowSystem.probesByName.get(PipeFlowSystem.ActorNames.TaskScheduler) shouldBe defined
    }
  }

  it should "create an actor when a node builder is scheduled" in {
    withPipeFlowSystem { pipeflowSystem =>
      pipeflowSystem.probes shouldBe empty
      pipeflowSystem.schedule("R//PT1H") { _ => Task("id") }
      pipeflowSystem.probes should have size 1
    }
  }

  it should "throw an exception when a repeating interval fails to parse in schedule" in {
    withPipeFlowSystem { pipeflowSystem =>
      a[ScheduleRepeatingIntervalException] shouldBe thrownBy {
        pipeflowSystem.schedule("X//PT1H") { _ => Task("id") }
      }
    }
  }

  it should "send a node creation event to the task scheduler" in {
    withPipeFlowSystem { pipeflowSystem =>
      val taskScheduler = pipeflowSystem.probesByName(PipeFlowSystem.ActorNames.TaskScheduler)
      val task = Task("id")
      pipeflowSystem.schedule(task)
      taskScheduler.expectMsg(NodeCreated(task))
    }
  }
}

trait PipeFlowSystemFixtures {
  this: TestKit =>

  protected implicit val clock: Clock = Clock.systemUTC()

  protected val config: Config = ConfigFactory.parseString("")

  trait ProbeMaker extends ActorMaker {
    lazy val probes: mutable.ArrayBuffer[TestProbe] = mutable.ArrayBuffer.empty
    lazy val probesByName: mutable.Map[String, TestProbe] = mutable.Map.empty

    override protected def actorOf(props: Props): ActorRef = {
      val probe = TestProbe()(system)
      probes += probe
      probe.ref
    }

    override def actorOf(props: Props, name: String): ActorRef = {
      val probe = TestProbe()(system)
      probesByName(name) = probe
      probe.ref
    }
  }

  def withPipeFlowSystem(testBlock: (PipeFlowSystem with ProbeMaker) => Any): Any = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val pipeflowSystem = new PipeFlowSystem("test", config, system) with ProbeMaker
    testBlock(pipeflowSystem)
  }
}