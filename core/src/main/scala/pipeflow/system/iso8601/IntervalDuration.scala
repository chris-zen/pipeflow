package pipeflow.system.iso8601

import java.time.{Duration, Period}

import scala.util.{Failure, Success, Try}
import cats.syntax.either._
import cats.syntax.option._


object IntervalDuration {

  private val Regex = """(P.*?)(T.*)?""".r

  val Zero = IntervalDuration(Period.ZERO, Duration.ZERO)


  def apply(years: Int = 0, months: Int = 0, days: Int = 0,
            hours: Int = 0, minutes: Int = 0, seconds: Int = 0): IntervalDuration = {
    val dateDuration = Period.of(years, months, days)
    val timeDuration = Duration.parse(s"PT${hours}H${minutes}M${seconds}S")
    new IntervalDuration(dateDuration, timeDuration)
  }

  def apply(expression: String): Either[String, IntervalDuration] = {
    expression match {
      case Regex(dateDurationExpr, timeDurationExpr) =>
        for {
          dateDuration     <- parseDateDuration(expression, dateDurationExpr)
          timeDuration     <- parseTimeDuration(expression, timeDurationExpr)
          intervalDuration <- buildIntervalDuration(expression, dateDuration, timeDuration)
        } yield intervalDuration

      case _ => s"Wrong interval duration expression: $expression".asLeft
    }
  }

  private def parseDateDuration(expression: String, durationExpr: String): Either[String, Option[Period]] = {
    if (durationExpr.equalsIgnoreCase("P"))
      none.asRight
    else
      Either.catchNonFatal(Period.parse(durationExpr))
        .map(duration => duration.some)
        .leftMap(cause => s"Error parsing the expression '$expression': ${cause.getMessage}")
  }

  private def parseTimeDuration(expression: String, durationExprOrNull: String): Either[String, Option[Duration]] = {
    Option(durationExprOrNull).map { expr =>
      Either.catchNonFatal(Duration.parse(s"P$expr"))
        .map(duration => duration.some)
        .leftMap(cause => s"Error parsing the expression 'P$expr' into a Duration: ${cause.getMessage}")
    }.getOrElse(none.asRight)
  }

  private def buildIntervalDuration(expression: String,
                                    dateDuration: Option[Period],
                                    timeDuration: Option[Duration]): Either[String, IntervalDuration] = {

    if (dateDuration.isEmpty && timeDuration.isEmpty)
      s"Wrong interval duration expression '$expression'".asLeft
    else
      new IntervalDuration(
        dateDuration.getOrElse(Period.ZERO),
        timeDuration.getOrElse(Duration.ZERO)
      ).asRight
  }
}

/**
  * Represents a duration in an ISO8601 representation for a repeating interval.
  * @see [[https://en.wikipedia.org/wiki/ISO_8601#Durations ISO_8601 Durations]]
  *
  * As an example, {{{IntervalDuration("P3Y6M4DT12H30M5S")}}}
  * represents a duration of "three years, six months, four days, twelve hours, thirty minutes, and five seconds"
  */
case class IntervalDuration(dateDuration: Period, timeDuration: Duration)
