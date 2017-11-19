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

case class RepeatingInterval(
  recurrences: Long = RepeatingInterval.DefaultRecurrence,
  start: OffsetDateTime = RepeatingInterval.DefaultStart,
  duration: IntervalDuration = RepeatingInterval.DefaultDuration)
