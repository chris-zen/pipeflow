package pipeflow.system

import akka.actor.{ActorSystem, Props}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import org.mockito.Mockito.verify
import pipeflow.system.ActorMakerSpec.TestActorMaker

class ActorMakerSpec extends FlatSpec with Matchers with MockitoSugar {

  "An ActorMaker" should "create an actor" in {
    val mockedActorSystem = mock[ActorSystem]
    val maker = new TestActorMaker(mockedActorSystem)
    maker.actorOf(Props.empty)
    verify(mockedActorSystem).actorOf(Props.empty)
  }

  it should "create an actor with name" in {
    val mockedActorSystem = mock[ActorSystem]
    val maker = new TestActorMaker(mockedActorSystem)
    maker.actorOf(Props.empty, "test")
    verify(mockedActorSystem).actorOf(Props.empty, "test")
  }
}

object ActorMakerSpec {
  class TestActorMaker(protected val system: ActorSystem) extends ActorMaker {
    override def actorOf(props: Props) = super.actorOf(props)
    override def actorOf(props: Props, name: String) = super.actorOf(props, name)
  }
}
