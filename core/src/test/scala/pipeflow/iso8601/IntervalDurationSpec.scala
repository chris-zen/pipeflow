package pipeflow.iso8601

import java.time.{Duration, Period}

import org.scalatest.{FlatSpec, Matchers}

import cats.syntax.either._


class IntervalDurationSpec extends FlatSpec with Matchers {

  "An IntervalDuration" should "build from parameters" in {
    val intervalDuration = IntervalDuration(
      years = 1, months = 2, days = 3,
      hours = 4, minutes = 5, seconds = 6)

    intervalDuration.dateDuration shouldBe Period.of(1, 2, 3)
    intervalDuration.timeDuration shouldBe Duration.parse("PT4H5M6S")
  }

  it should "use the dateDuration for years, months and days, and the timeDuration for hours, minutes and seconds" in {
    val intervalDuration = IntervalDuration("P1Y2M3DT4H5M6S").right.get

    intervalDuration.dateDuration shouldBe Period.of(1, 2, 3)
    intervalDuration.timeDuration shouldBe Duration.parse("PT4H5M6S")
  }

  it should "parse P0D" in {
    IntervalDuration("P0D") shouldBe IntervalDuration.Zero.asRight
  }

  it should "parse PT1M" in {
    val expectedInterval = IntervalDuration(minutes = 1)
    IntervalDuration("PT1M") shouldBe expectedInterval.asRight
  }

  it should "fail to parse P" in {
    IntervalDuration("P").isLeft shouldBe true
  }

  it should "fail to parse PT" in {
    IntervalDuration("PT").isLeft shouldBe true
  }

  it should "fail to parse an empty string" in {
    IntervalDuration("").isLeft shouldBe true
  }

  it should "convert to string" in {
    val cases = Seq("PT1H", "P1D", "P1DT1H")
    val expected = cases.map(Right(_))

    cases.map(IntervalDuration(_).map(_.toString)) shouldBe expected
  }
}
