package pipeflow.system.iso8601

import java.time.{LocalDateTime, OffsetDateTime}

import org.scalatest.{FlatSpec, Matchers}

import scala.util.Success
import cats.syntax.either._
import cats.syntax.option._


class RepeatingIntervalSpec extends FlatSpec with Matchers {

  "A RepeatingInterval" should "parse R//P0D" in {
    val expectedInterval = RepeatingInterval()
    RepeatingInterval("R//P0D") shouldBe expectedInterval.asRight
  }

  it should "parse R52//P0D" in {
    val expectedInterval = RepeatingInterval(recurrences = Some(52L))
    RepeatingInterval("R52//P0D") shouldBe expectedInterval.asRight
  }

  it should "parse R//PT1M" in {
    val expectedInterval = RepeatingInterval(duration = IntervalDuration(minutes = 1))
    RepeatingInterval("R//PT1M") shouldBe expectedInterval.asRight
  }

  it should "parse R/2017-12-30T11:45+10:00/P1D" in {
    val expectedInterval = RepeatingInterval(
      start = OffsetDateTime.parse("2017-12-30T11:45+10:00").some,
      duration = IntervalDuration(days = 1))
    RepeatingInterval("R/2017-12-30T11:45+10:00/P1D") shouldBe expectedInterval.asRight
  }
}
