package pipeflow.dsl.requirements

import pipeflow.dsl.datarefs.DataRef

object DataRefRequirement {
  def apply[T](ref: T)(implicit evidence: DataRef.Evidence[T]): DataRefRequirement =
    new DataRefRequirement(evidence.dataRef(ref))
}

case class DataRefRequirement(dataRef: DataRef) extends Requirement
