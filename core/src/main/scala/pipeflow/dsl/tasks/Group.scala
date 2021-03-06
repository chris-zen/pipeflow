package pipeflow.dsl.tasks

import pipeflow.dsl.requirements.Requirement

object Group {
  def apply(id: String): Group = new Group(id)
}

case class Group private (
  id: String,
  name: Option[String] = None,
  requirements: Seq[Requirement] = Seq.empty,
  children: Seq[TaskLike] = Seq.empty
) extends TaskLike {

  def name(name: String): Group = this.copy(name = Some(name))

  def requires(requirement: Requirement): Group =
    this.copy(requirements = requirements :+ requirement)

  def requires(reqs: Seq[Requirement]): Group =
    this.copy(requirements = requirements ++ reqs)

  def requires[T](requirement: T)(implicit evidence: Requirement.Evidence[T]): Group =
    this.copy(requirements = requirements :+ evidence.requirement(requirement))

  def requires[T](reqs: Seq[T])(implicit evidence: Requirement.Evidence[T]): Group =
    this.copy(requirements = requirements ++ reqs.map(evidence.requirement))

  def withGroup(group: Group): Group = this.copy(children = children :+ group)
  def withGroups(groups: Seq[Group]): Group = this.copy(children = children ++ groups)

  def withTask(task: TaskLike): Group = this.copy(children = children :+ task)
  def withTasks(tasks: Seq[TaskLike]): Group = this.copy(children = children ++ tasks)
}
