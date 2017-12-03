package pipeflow.dsl.requirements

import pipeflow.dsl.nodes.{Group, Node, Task}
import pipeflow.dsl.requirements.Requirement.Evidence


case class NodeRequirement(node: Node) extends Requirement

object NodeRequirement {
  implicit object NodeEvidence extends Evidence[Node] {
    def requirement(from: Node): Requirement = NodeRequirement(from)
  }

  implicit object TaskEvidence extends Evidence[Task] {
    def requirement(from: Task): Requirement = NodeRequirement(from)
  }

  implicit object GroupEvidence extends Evidence[Group] {
    def requirement(from: Group): Requirement = NodeRequirement(from)
  }
}
