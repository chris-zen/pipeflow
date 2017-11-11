package pipeflow.core

import java.net.URI

trait DataRef {

  def uri: URI
}

object DataRef {

  case class Uri(uri: URI) extends DataRef

  implicit class UriFromString(uri: String) {
    def toUri: Uri = Uri(new URI(uri))
  }
}
