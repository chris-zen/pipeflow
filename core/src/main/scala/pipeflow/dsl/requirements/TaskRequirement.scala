package pipeflow.dsl.requirements

import pipeflow.dsl.tasks.{Group, TaskLike, Task}
import pipeflow.dsl.requirements.Requirement.Evidence


case class TaskRequirement(node: TaskLike) extends Requirement

object TaskRequirement {
  implicit object NodeEvidence extends Evidence[TaskLike] {
    def requirement(from: TaskLike): Requirement = TaskRequirement(from)
  }

  implicit object TaskEvidence extends Evidence[Task] {
    def requirement(from: Task): Requirement = TaskRequirement(from)
  }

  implicit object GroupEvidence extends Evidence[Group] {
    def requirement(from: Group): Requirement = TaskRequirement(from)
  }
}
