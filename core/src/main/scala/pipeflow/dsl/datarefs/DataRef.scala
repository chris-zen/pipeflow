package pipeflow.dsl.datarefs

import java.net.URI

trait DataRef {

  def uri: URI
}

object DataRef {

  trait Evidence[T] {
    def dataRef(from: T): DataRef
  }
}




