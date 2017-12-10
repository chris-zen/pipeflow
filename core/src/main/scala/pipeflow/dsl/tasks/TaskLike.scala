package pipeflow.dsl.tasks

import pipeflow.dsl.requirements.Requirement

trait TaskLike {
  def id: String
  def name: Option[String]
  def requirements: Seq[Requirement]
  def children: Seq[TaskLike]
}
