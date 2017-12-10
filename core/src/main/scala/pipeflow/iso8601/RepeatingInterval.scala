package pipeflow.iso8601

import java.time._

import scala.util.{Failure, Success, Try}
import cats.syntax.either._
import cats.syntax.option._


object RepeatingInterval {

  private val OffsetTimeZero = OffsetTime.parse("00:00Z")

  protected val DefaultDuration: IntervalDuration = IntervalDuration()

  private val Regex = """R([0-9]*)/(.*)/(P.*)""".r


  def apply(expression: String)(implicit clock: Clock): Either[String, RepeatingInterval] = {
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

  private def parseStart(expression: String, startExpr: String)(implicit clock: Clock): Either[String, Option[OffsetDateTime]] = {
    if (startExpr.isEmpty)
      none.asRight
    else
      Either.catchNonFatal(OffsetDateTime.parse(startExpr))
        .orElse(Either.catchNonFatal(OffsetTime.parse(startExpr).atDate(LocalDate.now(clock))))
        .orElse(Either.catchNonFatal(LocalDate.parse(startExpr).atTime(OffsetTimeZero)))
        .map(start => start.some)
        .leftMap(cause => s"Wrong format for the start part '$startExpr': ${cause.getMessage}")
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
  duration: IntervalDuration = RepeatingInterval.DefaultDuration) {

  override def toString: String = {
    val recurrencesStr = recurrences.map(_.toString).getOrElse("")
    val startStr = start.map(_.toString).getOrElse("")
    val durationStr = duration.toString
    s"R$recurrencesStr/$startStr/$durationStr"
  }
}
