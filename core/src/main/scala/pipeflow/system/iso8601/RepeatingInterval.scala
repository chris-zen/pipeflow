package pipeflow.system.iso8601

import java.time._

import scala.util.{Failure, Success, Try}
import cats.syntax.either._
import cats.syntax.option._


object RepeatingInterval {

  protected val DefaultDuration: IntervalDuration = IntervalDuration()

  private val Regex = """R([0-9]*)/(.*)/(P.*)""".r


  def apply(expression: String): Either[String, RepeatingInterval] = {
    expression match {
      case Regex(recurrencesExpr, startExpr, intervalExpr) =>
        for {
          recurrences      <- parseRecurrences(expression, recurrencesExpr)
          start            <- parseStart(expression, startExpr)
          intervalDuration <- IntervalDuration(intervalExpr)
        } yield new RepeatingInterval(recurrences, start, intervalDuration)

      case _ => s"Wrong format for the repeating interval '$expression'".asLeft
    }
  }

  private def parseRecurrences(expression: String, recExpr: String): Either[String, Option[Long]] = {
    if (recExpr.isEmpty)
      none.asRight
    else
      recExpr.toLong.some.asRight
  }

  private def parseStart(expression: String, startExpr: String): Either[String, Option[OffsetDateTime]] = {
    if (startExpr.isEmpty)
      none.asRight
    else
      Try(OffsetDateTime.parse(startExpr)) match {
        case Success(start) => start.some.asRight
        case Failure(cause) => s"Wrong format for the start part '$startExpr'".asLeft
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
  recurrences: Option[Long] = None,
  start: Option[OffsetDateTime] = None,
  duration: IntervalDuration = RepeatingInterval.DefaultDuration)
