package pipeflow.dsl.tasks

import org.scalatest.{FlatSpec, Matchers}
import pipeflow.dsl.datarefs.Uri._
import pipeflow.dsl.datarefs.Uri
import pipeflow.dsl.actions.{Action, Closure}
import pipeflow.dsl.requirements.{DataRefRequirement, TaskRequirement}
import pipeflow.dsl.requirements.TaskRequirement._
import pipeflow.dsl.tasks.TaskSpec.DummyAction


class TaskSpec extends FlatSpec with Matchers {

  "A Task" should "build with id" in {
    Task("id").id shouldBe "id"
  }

  it should "build with name" in {
    Task("id").name("name").name shouldBe Some("name")
  }

  it should "build with a required node" in {
    val node = Task("req")
    Task("id").requires(node).requirements shouldBe Seq(TaskRequirement(node))
  }

  it should "build with a requirement" in {
    val node = Task("req")
    Task("id").requires(TaskRequirement(node)).requirements shouldBe Seq(TaskRequirement(node))
  }

  it should "build with requirements" in {
    val requirements = Seq(Task("req1"), Task("req2")).map(TaskRequirement.apply)
    Task("id").requires(requirements).requirements shouldBe requirements
  }

  it should "build with an input" in {
    val ref = "uri"
    Task("id").input(ref).requirements shouldBe Seq(DataRefRequirement(ref))
  }

  it should "build with inputs" in {
    val refs = Seq("uri1", "uri2")
    Task("id").inputs(refs).requirements shouldBe refs.map(ref => DataRefRequirement(ref))
  }

  it should "provide only inputs" in {
    val refs = Seq("uri1", "uri2")
    val requirement = Task("req")
    val task = Task("id")
      .inputs(refs)
      .requires(requirement)

    task.inputs shouldBe refs.map(Uri.apply)
  }

  it should "build with an output" in {
    val ref = "uri"
    Task("id").output(ref).outputs shouldBe Seq(Uri(ref))
  }

  it should "build with outputs" in {
    val refs = Seq("uri1", "uri2")
    Task("id").outputs(refs).outputs shouldBe refs.map(Uri.apply)
  }

  it should "build with an action" in {
    val action = new DummyAction
    Task("id").action(action).action shouldBe Some(action)
  }

  it should "build with an action from a closure" in {
    import pipeflow.dsl.actions.Closure._
    val action = () => {}
    Task("id").action(action).action shouldBe Some(Closure(action))
  }
}

object TaskSpec {
  class DummyAction extends Action
}
