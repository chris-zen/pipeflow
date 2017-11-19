package pipeflow.system.iso8601

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class RepeatingIntervalSpec extends FlatSpec with Matchers {

  "A RepeatingInterval" should "parse R//P0D" in {
    val expectedInterval = RepeatingInterval()
    RepeatingInterval("R//P0D") shouldBe Success(expectedInterval)
  }

  it should "parse R//PT1M" in {
    val expectedInterval = RepeatingInterval(duration = IntervalDuration(minutes = 1))
    RepeatingInterval("R//PT1M") shouldBe Success(expectedInterval)
  }
}
