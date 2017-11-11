package pipeflow.core

case class Task(
  id: String,
  name: Option[String] = None,
  requires: Seq[Requirement] = Seq.empty,
  outputs: Seq[DataRef] = Seq.empty,
  action: Option[Action] = None
) extends Node {

  def children: Seq[Node] = Seq.empty

  def name(name: String): Task = this.copy(name = Some(name))

  def requires(node: Node): Task = this.copy(requires = requires :+ NodeReq(node))
  def requires(requirement: Requirement): Task = this.copy(requires = requires :+ requirement)
  def requires(requirements: Seq[Requirement]): Task = this.copy(requires = requires ++ requirements)

  def input(ref: DataRef): Task = this.copy(requires = requires :+ DataReq(ref))
  def inputs(refs: Seq[DataRef]): Task = this.copy(requires = requires ++ refs.map(DataReq))
  def inputs: Seq[DataRef] = requires.flatMap(_ match {
    case DataReq(dataRef) => Some(dataRef)
    case _ => None
  })

  def output(ref: DataRef): Task = this.copy(outputs = outputs :+ ref)
  def outputs(refs: Seq[DataRef]): Task = this.copy(outputs = outputs ++ refs)

  def action(action: Action): Task = this.copy(action = Some(action))
}
