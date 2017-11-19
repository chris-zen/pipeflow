package pipeflow.system.iso8601

import java.time.{Duration, Period}

import scala.util.{Failure, Success, Try}


object IntervalDuration {

  class IntervalDurationParsingException(expression: String, cause: Throwable)
    extends Exception(s"Wrong interval duration expression: $expression", cause)

  class IntervalDurationFormatException(expression: String)
    extends Exception(s"Wrong interval duration expression: $expression")

  private val Regex = """(P.*?)(T.*)?""".r

  protected val DefaultDateDuration: Period = Period.ZERO
  protected val DefaultTimeDuration: Duration = Duration.ZERO


  def apply(years: Int = 0, months: Int = 0, days: Int = 0,
            hours: Int = 0, minutes: Int = 0, seconds: Int = 0): IntervalDuration = {
    val dateDuration = Period.of(years, months, days)
    val timeDuration = Duration.parse(s"PT${hours}H${minutes}M${seconds}S")
    new IntervalDuration(dateDuration, timeDuration)
  }

  def apply(expression: String): Try[IntervalDuration] = {
    expression match {
      case Regex(dateDurationExpr, timeDurationExpr) =>
        for {
          dateDuration <- parseDateDuration(expression, dateDurationExpr)
          timeDuration <- parseTimeDuration(expression, timeDurationExpr)
        } yield new IntervalDuration(dateDuration, timeDuration)

      case _ => Failure(new IntervalDurationFormatException(expression))
    }
  }

  private def parseDateDuration(expression: String, durationExpr: String): Try[Period] = {
    if (durationExpr.equalsIgnoreCase("P"))
      Success(DefaultDateDuration)
    else
      Try(Period.parse(durationExpr)).recoverWith {
        case cause => Failure(new IntervalDurationParsingException(expression, cause))
      }
  }

  private def parseTimeDuration(expression: String, durationExpr: String): Try[Duration] = {
    Option(durationExpr) match {
      case Some(_) => Try(Duration.parse(s"P$durationExpr")).recoverWith {
        case cause => Failure(new IntervalDurationParsingException(expression, cause))
      }
      case None => Success(DefaultTimeDuration)
    }
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
