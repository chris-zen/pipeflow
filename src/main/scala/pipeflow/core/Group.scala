package pipeflow.core

case class Group(
  id: String,
  name: Option[String] = None,
  requires: Seq[Requirement] = Seq.empty,
  children: Seq[Node] = Seq.empty
) extends Node {

  def requires(requirement: Requirement): Group = this.copy(requires = requires :+ requirement)
  def requires(requirements: Seq[Requirement]): Group = this.copy(requires = requires ++ requirements)

  def withGroup(group: Group) = this.copy(children = children :+ group)
  def withGroups(groups: Seq[Group]) = this.copy(children = children ++ groups)

  def withTask(task: Task): Group = this.copy(children = children :+ task)
  def withTasks(tasks: Seq[Task]): Group = this.copy(children = children ++ tasks)

  def withNodes(nodes: Seq[Node]): Group = this.copy(children = children ++ nodes)
}
