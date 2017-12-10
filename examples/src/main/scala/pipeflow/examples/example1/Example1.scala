package pipeflow.examples.example1

import java.time.{Clock, LocalDateTime}
import java.time.format.DateTimeFormatter
import scala.collection.JavaConverters._

import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import pipeflow.dsl.actions.Closure._
import pipeflow.dsl.datarefs.Uri._
import pipeflow.dsl.requirements.TaskRequirement._
import pipeflow.dsl.tasks.{Group, TaskLike, Task}
import pipeflow.system.PipeFlowSystem


/**
  * Hierarchy:
  *
  * preprocessing-2017-11-18T00:10
  * +- cleaning-2017-11-18T00:10
  * |  +- cleaning-it-2017-11-18T00:10
  * |  +- cleaning-fr-2017-11-18T00:10
  * +- aggregation-2017-11-18T00:10
  *
  * Topological order:
  *
  * preprocessing[ cleaning[ it, fr ] -> aggregation ]
  *
  */
object Example1 extends App {

  private val logger = LoggerFactory.getLogger(this.getClass.getName.split("[.$]").last)

  private implicit val clock: Clock = Clock.systemUTC()

  import scala.concurrent.ExecutionContext.Implicits.global

  logger.info("Starting application ...")

  val config = ConfigFactory.parseString(
    """
      |countries = ["it", "fr"]
    """.stripMargin)

  val countries = config.getStringList("countries").asScala

  val system = PipeFlowSystem("example1")

  scala.sys.addShutdownHook {
    logger.info("Process shutdown")
    system.shutdown()
  }

  system.schedule("R/T00:30Z/PT1H") { dateTime =>
    buildPreprocessing(dateTime, countries)
  }

  system.awaitTermination()

  logger.info("Application finished ...")


  /**
    * Build the preprocessing pipeline
    *
    * @param dateTime the date/time being processed
    * @param countries the list of country codes to process
    * @return The group of tasks for preprocessing the data on a given date/time and set of countries
    */
  private[example1] def buildPreprocessing(dateTime: LocalDateTime, countries: Seq[String]): Group = {
    val cleaning = buildCleaning(dateTime, countries)

    val aggregation = buildAggregation(dateTime, cleaning)

    Group(s"preprocessing-${dateTime.asId}")
      .name("Data preprocessing")
      .withGroup(cleaning)
      .withTask(aggregation)
  }

  /**
    * Build the cleaning group of tasks
    *
    * @param dateTime the date/time being processed
    * @param countries the list of country codes to process
    * @return The group of tasks with for cleaning data on a given date/time and set of countries
    */
  private[example1] def buildCleaning(dateTime: LocalDateTime, countries: Seq[String]): Group = {
    val dateTimePath = dateTime.asHourlyPath
    Group(s"cleaning-${dateTime.asId}").withTasks {
      for (country <- countries)
        yield Task(s"cleaning-$country-${dateTime.asId}")
          .name(s"Cleaning source data for country $country")
          .input(s"s3://data/source/$country/$dateTimePath")
          .output(s"s3://data/clean/$country/$dateTimePath")
          .action { () =>
            println(s"Cleaning data for the country '$country' on $dateTime ...")
          }
    }
  }

  /**
    * Build the aggregation task
    *
    * @param dateTime the date/time being processed
    * @param cleaning the node for cleaning the data this node depends on
    * @return The task node that aggregates the data per user for all the countries on a given date/time
    */
  private[example1] def buildAggregation(dateTime: LocalDateTime, cleaning: TaskLike): Task = {
    Task(s"aggregation-${dateTime.asId}")
      .name("Per user aggregation for all the countries")
      .input(s"s3://data/user-info/${dateTime.asDailyPath}")
      .output(s"s3://data/aggregation/${dateTime.asHourlyPath}")
      .requires(cleaning)
      .action { () =>
        println(s"Aggregating data per user on $dateTime ...")
      }
  }

  private[example1] implicit class DateTimeId(dateTime: LocalDateTime) {
    private val IdFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd-HH")
    def asId: String = dateTime.format(IdFormatter)
    def asDailyPath: String = f"${dateTime.getYear}/${dateTime.getMonthValue}%02d/${dateTime.getDayOfMonth}%02d"
    def asHourlyPath: String = f"$asDailyPath/${dateTime.getHour}%02d"
  }
}
