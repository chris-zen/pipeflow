package pipeflow.system.iso8601

import java.time.{Duration, Period}

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success

class IntervalDurationSpec extends FlatSpec with Matchers {

  "An IntervalDuration" should "build from parameters" in {
    val intervalDuration = IntervalDuration(
      years = 1, months = 2, days = 3,
      hours = 4, minutes = 5, seconds = 6)

    intervalDuration.dateDuration shouldBe Period.of(1, 2, 3)
    intervalDuration.timeDuration shouldBe Duration.parse("PT4H5M6S")
  }

  it should "parse P0D" in {
    val expectedInterval = IntervalDuration()
    IntervalDuration("P0D") shouldBe Success(expectedInterval)
  }

  it should "parse PT1M" in {
    val expectedInterval = IntervalDuration(minutes = 1)
    IntervalDuration("PT1M") shouldBe Success(expectedInterval)
  }
}
