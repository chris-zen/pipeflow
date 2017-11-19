package pipeflow.examples

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import pipeflow.dsl.datarefs.Uri._
import pipeflow.dsl.actions.Closure._
import pipeflow.dsl.nodes.{Group, Node, Task}
import pipeflow.system.PipeFlowSystem


/**
  * Hierarchy:
  *
  * preprocessing
  * +- cleaning-2017-11-18T00:10:20
  * |  +- cleaning-it-2017-11-18T00:10:20
  * |  +- cleaning-fr-2017-11-18T00:10:20
  * +- aggregation
  *
  * Topological order:
  *
  * preprocessing[ cleaning[ it, fr ] -> aggregation ]
  *
  */
object Example1 extends App {

  val Countries = Seq("it", "fr")

  val system = PipeFlowSystem("example")
  system.schedule("R//PT1M") { dateTime =>
    buildPreprocessing(dateTime, Countries)
  }

  system.run()

  def buildPreprocessing(dateTime: LocalDateTime, countries: Seq[String]): Group = {
    val cleaning = buildCleaning(dateTime, Countries)

    val aggregation = buildAggregation(dateTime, Countries, cleaning)

    Group("preprocessing")
      .name("Data preprocessing")
      .withGroup(cleaning)
      .withTask(aggregation)
  }

  def buildCleaning(dateTime: LocalDateTime, countries: Seq[String]): Group = {
    val timePartition = f"${dateTime.getYear}/${dateTime.getMonthValue}%02d/${dateTime.getDayOfMonth}%02d"

    Group(s"cleaning-${dateTime.asId}").withTasks {
      for (country <- countries)
        yield Task(s"cleaning-$country-${dateTime.asId}")
          .name(s"Cleaning of source data for country $country")
          .input(s"s3://source/$country/$timePartition")
          .output(s"s3://clean/$country/$timePartition")
          .action { () =>
            println(s"Cleaning data for the country '$country' on $dateTime ...")
          }
    }
  }

  def buildAggregation(dateTime: LocalDateTime, countries: Seq[String], cleaning: Node): Task = {
    Task(s"aggregation-${dateTime.asId}")
      .name("Per user aggregation for all the countries")
      .input("s3://y/in")
      .output("s3://y/out/year=2016/day=6")
      .requires(cleaning)
  }

  implicit class DateTimeId(dateTime: LocalDateTime) {
    def asId: String = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
  }
}
