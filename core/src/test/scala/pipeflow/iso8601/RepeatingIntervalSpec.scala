package pipeflow.iso8601

import java.time._

import cats.syntax.either._
import cats.syntax.option._
import org.scalatest.{FlatSpec, Matchers}


class RepeatingIntervalSpec extends FlatSpec with Matchers {

  implicit val clock: Clock = Clock.fixed(OffsetDateTime.parse("2017-12-22T00:00Z").toInstant, ZoneOffset.UTC)

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

  it should "parse R/11:45Z/P1D" in {
    val expectedInterval = RepeatingInterval(
      start = OffsetTime.parse("11:45Z").atDate(LocalDate.now(clock)).some,
      duration = IntervalDuration(days = 1))
    RepeatingInterval("R/11:45Z/P1D") shouldBe expectedInterval.asRight
  }

  it should "parse R/11:45/P1D" in {
    val expectedInterval = RepeatingInterval(
      start = LocalDate.parse("2017-12-30").atTime(OffsetTime.parse("00:00Z")).some,
      duration = IntervalDuration(days = 1))
    RepeatingInterval("R/2017-12-30/P1D") shouldBe expectedInterval.asRight
  }

  it should "convert to string" in {
    val cases = Seq("R//PT1S", "R2//P1DT1H", "R/2017-08-14T00:08+10:00/P1D")
    val expected = cases.map(Right(_))

    cases.map(RepeatingInterval(_).map(_.toString)) shouldBe expected
  }
}
