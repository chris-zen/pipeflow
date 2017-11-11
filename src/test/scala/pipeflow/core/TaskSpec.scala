package pipeflow.core

import org.scalatest.{FlatSpec, Matchers}
import pipeflow.core.DataRef.UriFromString
import pipeflow.core.TaskSpec.DummyAction

class TaskSpec extends FlatSpec with Matchers {

  "A Task" should "build with id" in {
    Task("id").id shouldBe "id"
  }

  it should "build with name" in {
    Task("id").name("name").name shouldBe Some("name")
  }

  it should "build with a required node" in {
    val node = Task("req")
    Task("id").requires(node).requires shouldBe Seq(NodeReq(node))
  }

  it should "build with a requirement" in {
    val node = Task("req")
    Task("id").requires(NodeReq(node)).requires shouldBe Seq(NodeReq(node))
  }

  it should "build with requirements" in {
    val requirements = Seq(Task("req1"), Task("req2")).map(NodeReq)
    Task("id").requires(requirements).requires shouldBe requirements
  }

  it should "build with an input" in {
    val ref = "uri".toUri
    Task("id").input(ref).requires shouldBe Seq(DataReq(ref))
  }

  it should "build with inputs" in {
    val refs = Seq("uri1".toUri, "uri2".toUri)
    Task("id").inputs(refs).requires shouldBe refs.map(DataReq)
  }

  it should "provide only inputs" in {
    val refs = Seq("uri1".toUri, "uri2".toUri)
    val requirement = Task("req")
    val task = Task("id")
      .inputs(refs)
      .requires(requirement)

    task.inputs shouldBe refs
  }

  it should "build with an output" in {
    val ref = "uri".toUri
    Task("id").output(ref).outputs shouldBe Seq(ref)
  }

  it should "build with outputs" in {
    val refs = Seq("uri1".toUri, "uri2".toUri)
    Task("id").outputs(refs).outputs shouldBe refs
  }

  it should "build with an action" in {
    val action = new DummyAction
    Task("id").action(action).action shouldBe Some(action)
  }
}

object TaskSpec {
  class DummyAction extends Action
}
