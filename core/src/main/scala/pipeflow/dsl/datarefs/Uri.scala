package pipeflow.dsl.datarefs

import java.net.URI

import pipeflow.dsl.datarefs.DataRef.Evidence

case class Uri(uri: URI) extends DataRef

object Uri {

  def apply(uri: String): Uri = new Uri(new URI(uri))

  implicit object StringEvidence extends Evidence[String] {
    def dataRef(from: String): DataRef = Uri(new URI(from))
  }

  implicit object UriEvidence extends Evidence[URI] {
    def dataRef(from: URI): DataRef = Uri(from)
  }
}
