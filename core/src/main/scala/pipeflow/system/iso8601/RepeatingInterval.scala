package pipeflow.system.iso8601

import java.time._

import scala.util.{Failure, Success, Try}


object RepeatingInterval {
  class RepeatingIntervalFormatException(expression: String)
    extends Exception(s"Wrong repeating interval expression: $expression")

  class RepeatingIntervalRecurrenceException(expression: String, cause: Throwable)
    extends Exception(s"Wrong recurrence expression while parsing the repeating interval: $expression", cause)

  class RepeatingIntervalStartException(expression: String, cause: Throwable)
    extends Exception(s"Wrong start expression while parsing the repeating interval: $expression", cause)


  protected val DefaultRecurrence: Long = 0L
  protected val DefaultStart: OffsetDateTime = LocalDateTime.MIN.atOffset(ZoneOffset.UTC)
  protected val DefaultDuration: IntervalDuration = IntervalDuration()

  private val Regex = """R([0-9]*)/(.*)/(P.*)""".r


  def apply(expression: String): Try[RepeatingInterval] = {
    expression match {
      case Regex(recurrencesExpr, startExpr, intervalExpr) =>
        for {
          recurrences      <- parseRecurrences(expression, recurrencesExpr)
          start            <- parseStart(expression, startExpr)
          intervalDuration <- IntervalDuration(intervalExpr)
        } yield new RepeatingInterval(recurrences, start, intervalDuration)

      case _ => Failure(new RepeatingIntervalFormatException(expression))
    }
  }

  private def parseRecurrences(expression: String, recExpr: String): Try[Long] = {
    if (recExpr.isEmpty)
      Success(DefaultRecurrence)
    else
      Try(recExpr.toLong).recoverWith {
        case cause => Failure(new RepeatingIntervalRecurrenceException(expression, cause))
      }
  }

  private def parseStart(expression: String, startExpr: String): Try[OffsetDateTime] = {
    if (startExpr.isEmpty)
      Success(DefaultStart)
    else
      Try(OffsetDateTime.parse(startExpr)).recoverWith {
        case cause => Failure(new RepeatingIntervalStartException(expression, cause))
      }
  }
}

/**
  * Represents a subset of the ISO8601 repeating interval specification.
  * @see [[https://en.wikipedia.org/wiki/ISO_8601#Repeating_intervals ISO_8601 Repeating intervals]]
  *
  * Specifically it only supports time intervals defined as `<start>/<duration>`.
  *
  * As an example, {{{RepeatingInterval("R5/2008-03-01T13:00Z/PT2H")}}}
  * represents 5 repetitions starting at 2008-03-01 13:00 UTC that repeats every 2 hours.
  */
case class RepeatingInterval(
  recurrences: Long = RepeatingInterval.DefaultRecurrence,
  start: OffsetDateTime = RepeatingInterval.DefaultStart,
  duration: IntervalDuration = RepeatingInterval.DefaultDuration)
