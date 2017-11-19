package pipeflow.dsl.nodes

import pipeflow.dsl.actions.Action
import pipeflow.dsl.datarefs.DataRef
import pipeflow.dsl.requirements.{DataRefRequirement, NodeRequirement, Requirement}

object Task {
  def apply(id: String): Task = new Task(id)
}

case class Task private (
  id: String,
  name: Option[String] = None,
  requires: Seq[Requirement] = Seq.empty,
  outputs: Seq[DataRef] = Seq.empty,
  action: Option[Action] = None
) extends Node {

  def children: Seq[Node] = Seq.empty

  def name(name: String): Task = this.copy(name = Some(name))

  def requires(node: Node): Task = this.copy(requires = requires :+ NodeRequirement(node))
  def requires(requirement: Requirement): Task = this.copy(requires = requires :+ requirement)
  def requires(requirements: Seq[Requirement]): Task = this.copy(requires = requires ++ requirements)

  def input[T](ref: T)(implicit evidence: DataRef.Evidence[T]): Task =
    this.copy(requires = requires :+ DataRefRequirement(evidence.dataRef(ref)))

  def inputs[T](refs: Seq[T])(implicit evidence: DataRef.Evidence[T]): Task =
    this.copy(requires = requires ++ refs.map(ref => DataRefRequirement(evidence.dataRef(ref))))

  def inputs: Seq[DataRef] = requires.flatMap(_ match {
    case DataRefRequirement(dataRef) => Some(dataRef)
    case _ => None
  })

  def output[T](ref: T)(implicit evidence: DataRef.Evidence[T]): Task =
    this.copy(outputs = outputs :+ evidence.dataRef(ref))

  def outputs[T](refs: Seq[T])(implicit evidence: DataRef.Evidence[T]): Task =
    this.copy(outputs = outputs ++ refs.map(ref => evidence.dataRef(ref)))

  def action(action: Action): Task = this.copy(action = Some(action))

  def action[T](action: T)(implicit evidence: Action.Evidence[T]): Task =
    this.copy(action = Some(evidence.action(action)))
}
