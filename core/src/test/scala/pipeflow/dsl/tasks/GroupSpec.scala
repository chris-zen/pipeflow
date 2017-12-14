package pipeflow.dsl.tasks

import org.scalatest.{FlatSpec, Matchers}

import pipeflow.dsl.requirements.TaskRequirement


class GroupSpec extends FlatSpec with Matchers {

  "A Group" should "build with a requirement" in {
    val node = Task("req")
    Group("id").requires(TaskRequirement(node)).requirements shouldBe Seq(TaskRequirement(node))
  }

  it should "build with name" in {
    Group("id").name("name").name shouldBe Some("name")
  }

  it should "build with requirements" in {
    val requirements = Seq(Task("req1"), Task("req2")).map(TaskRequirement.apply)
    Group("id").requires(requirements).requirements shouldBe requirements
  }

  it should "build with a group" in {
    val group = Group("child")
    val parent = Group("parent").withGroup(group)
    parent.children shouldBe Seq(group)
  }

  it should "build with groups" in {
    val children = Seq(Group("child1"), Group("child2"))
    val parent = Group("parent").withGroups(children)
    parent.children shouldBe children
  }

  it should "build with a task" in {
    val task = Task("child")
    val parent = Group("parent").withTask(task)
    parent.children shouldBe Seq(task)
  }

  it should "build with tasks" in {
    val children = Seq(Task("child1"), Task("child2"))
    val parent = Group("parent").withTasks(children)
    parent.children shouldBe children
  }
}
