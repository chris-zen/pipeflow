package pipeflow.dsl.nodes

import pipeflow.dsl.requirements.Requirement

trait Node {
  def id: String
  def name: Option[String]
  def requires: Seq[Requirement]
  def children: Seq[Node]
}
