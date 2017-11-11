package pipeflow.core

trait Requirement {
  // def children: Iterable[Requirement] = Iterable.empty
}

//case class SomeOf(reqs: Iterable[Requirement], min: Int) extends Requirement
//case class Weak(req: Requirement) extends Requirement

case class NodeReq(node: Node) extends Requirement
case class DataReq(dataRef: DataRef) extends Requirement
