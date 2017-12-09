# Basic Concepts

Following you will find some concepts that are important to understand the DSL and the overall design of the system.

## Task

A `Task` represents some computation that might require some input data and might generate some output data.

The most important attributes for a task are:
- An identifier that is unique across all the tasks managed by the system.
- A descriptive name of its function for visualization purposes
- A set of references to input data
- A set of references to output data
- A set of requirements that need to be met before its execution
- An action representing the computation that needs to be executed for the task to be completed

The only required attributes for a task are the identifier and the action.

Following there is an example of how a task can be defined with the DSL:

```scala
import java.time.LocalDateTime
import pipeflow.dsl.datarefs.Uri._
import pipeflow.dsl.actions.Closure._
import pipeflow.dsl.nodes.Task

val dateTime = LocalDateTime.parse("2017-08-12T01:00")

val cleaning = Task(s"cleaning-$dateTime")
                 .action { () => println(s"Cleaning data for $dateTime ...")}

val aggregation = Task(s"aggregation-$dateTime")
                    .name("Per user aggregation for all the countries")
                    .input(s"s3://data/user-info/$dateTime")
                    .output(s"s3://data/aggregation/$dateTime")
                    .requires(cleaning)
                    .action { () => println(s"Aggregating data per user on $dateTime ...") }
```

### Requirements

Tasks and groups of them, can depend on each other's completion before its execution can start,
 but also on other kind of conditions, such as the existence of external data sources,
 some event to happen, and so on and so forth.
 The DSL defines the trait `Requierement` to represent such node requirement for execution.
 And it also provides several specialisations, such as the `NodeRequirement` that represents
 a requirement on another node to complete, or the `DataRefRequirement` that represents a requirement on a data source to exist.

The following examples show how such requirements can be defined in different ways:

```scala
import pipeflow.dsl.nodes.Task

// task1 needs to execute and complete before task2 can start
val task1 = Task("task1")
val task2 = Task("task2").requires(NodeRequirement(task1))


// the dependency can be simplified importing an implicit conversion
import pipeflow.dsl.requirements.NodeRequirement._
val task3 = Task("task3").requires(task1)
```

The `DataRefRequirement` can also be specified explicitly using the method `input`,
 which expects any kind of `DataRef`, instead. One specialisation of `DataRef` provided in the DSL,
 is the `Uri`, that basically wraps a `java.lang.URI`.

See the following examples:

```scala
import pipeflow.dsl.nodes.Task

// task2 will depend on task1 to complete and s3://bucket/data to be available
val task1 = Task("task1")
val task2 = Task("task2")
  .requires(NodeRequirement(task1))
  .input(Uri(new URI("s3://bucket/data")))
```

The previous declaration can be simplified by importing some implicit conversions:

```scala
import pipeflow.dsl.requirements.NodeRequirement._
import pipeflow.dsl.datarefs.Uri._
val task1 = Task("task1")
val task2 = Task("task2")
  .requires(task1)
  .input("s3://bucket/data")
```

### Output

Tasks can define output data references. Even when the system doesn't have a way to enforce
 that the task logic actually creates such output, it helps two kind of purposes:

- to help defining dependencies between nodes implicitly, so even when two distant tasks don't depend
  on each other explicitly (using the `requires` method), if the input of one of them, references
  the output of the other one, then such requirement will be created.
- to help the task scheduler to know if a task has been completed already, as tasks defining an output
  will check whether its output exists or not, and skip its execution if the output data already exists,
  or mark it as failed after execution if the output doesn't exist as expected.

Example:
```scala
import pipeflow.dsl.nodes.Task
import pipeflow.dsl.datarefs.Uri._
val task1 = Task("task1")
  .output("s3://bucket/data")
    
val task2 = Task("task2")
  .input("s3://bucket/data")
```

### Action

An `Action` represents the actual logic that needs to be run for a task to be completed.
 There will be several specialisations for that trait depending on the nature of the action.
 Initially there is only one specialisation to use a closure, but the idea is to have other specialisations
 such as: `DockerRun`, `SparkSubmit`, `SparkClosure`, ...

Actions are related to the execution subsystem in certain way, as they need to be run in different hosts
 from the one running the PipeFlow system. But that's a topic that needs to be worked on quite a bit still.

### Parameters

There is no such specific mechanism to represent parameters as it is the case of Luigi right now. The plan is that
 Scala is powerful enough to not having to provide it. Just by using the language type system, and functional
 programming or object oriented paradigms, we can have pipelines that are modular, re-usable, testable, and so on and so forth.
 
The examples provided with this project show how the DSL can be used, with the Scala language and
 the standard software engineering practices, to build batch pipelines that look like micro-services,
 as an standard software project, with conventional configuration management, dependency injection,
 continuous integration/delivery, testing, and so on and so forth.
 
## Group

A `Group` allows to put together other groups or tasks, which allows to define a hierarchy of nodes (either groups or tasks).

For example, imagine that we have a preprocessing workflow with an step for cleaning up some data before it can be processed,
 and we need to do that for three different countries (es, it, fr).
 We could create one task per country for the cleaning step (`cleaning-es`, `cleaning-it` and `cleaning-fr`),
  and create a group with id `cleaning` to contain them all like:
  
```scala
import pipeflow.dsl.datarefs.Uri._
import pipeflow.dsl.actions.Closure._
import pipeflow.dsl.nodes.{Group, Task}

val countries = Seq("es", "it", "fr")

val cleaning = Group(s"cleaning").withTasks {
  for (country <- countries)
    yield Task(s"cleaning-$country")
      .name(s"Cleaning source data for country $country")
      .input(s"s3://data/source/$country")
      .output(s"s3://data/clean/$country")
      .action { () => println(s"Cleaning data for the country '$country' ...") }
}

val preprocessing = Group("preprocessing")
  .name("Data preprocessing")
  .withGroup(cleaning)
```

The previous example would create a hierarchy of nodes for preprocessing like:

```
preprocessing
+- cleaning
   +- cleaning-es
   +- cleaning-it
   +- cleaning-fr
```

Both the `Task` and the `Group` classes are a `Node`, and have some properties in common:
- they need to be uniquely identified by an id
- they have a descriptive name
- they can have dependencies on other `Node`s
- they can be used whenever a graph of tasks is needed in the API

## Jobs

A single task represent a unit of execution, that depends on other tasks or events to complete before it can start.
 But how and when are the task graphs created, can depend on other factors, such as a periodic schedule,
 or an external event (an SQS event, some Kafka message, an API call, ...). 

A `Job` represent a task or group of tasks, that are build dynamically according to some parameters or configuration,
 and triggered in response to some external event/s.
 
They represent the main entry point for the system, and the same way as the `Group`'s and `Task`'s
 they are defined declaratively with the DSL.

Examples:

```scala
Job("job1")
  .name("The job 1: a single shoot run")
  .builder({
    case SystemStart => buildJob1()
  })
  .onTaskComplete({ })
  .onTaskFailure({ })
  .onComplete({ })
  .onFailure({ })

Job("job2")
  .name("The job 2: A periodic running job")
  .schedule("R/00:20Z/PT1H")
  .builder {
    case Schedule(scheduledTime) => buildJob2(scheduledTime)
  }

Job("job3")
  .name("The job 3: A job triggered by events")
  .builder {
    case MyEvent(details) => buildJob3(details)
  }
```

As you can imagine by following the example, a single job can build tasks on several criteria at the same time:

```scala
Job("job-mix")
  .name("A mixed strategy to build tasks")
  .schedule("R/00:20Z/PT1H")
  .builder {
    case SystemStart => buildJobStart()
    case Schedule(scheduledTime) => buildJobSchedule(scheduledTime)
    case MyEvent(details) => buildJobOnEvent(details)
  }
```

## Application

One of the main ideas of PipeFlow, is to manage pipelines as a regular distributed application,
 and not having to depend on an specific way to deploy (such as it is the case of AirFlow)
 or on a central server for coordination (such as it is the case with Luigi).
 
The application represents the main entry point, where configuration is loaded, and the system is set up.
 PipeFlow provides, not only the DSL, but also an API to make that very simple.

The most important abstraction is the `PipeFlowSystem` that can be used like in the following example:

```scala
import org.slf4j.LoggerFactory
import com.typesafe.config.{Config, ConfigFactory}
import pipeflow.dsl.actions.Closure._
import pipeflow.dsl.datarefs.Uri._
import pipeflow.dsl.requirements.NodeRequirement._
import pipeflow.dsl.nodes.{Group, Node, Task}
import pipeflow.dsl.jobs.Job
import pipeflow.system.PipeFlowSystem

object MyApp extends App {

  private val logger = LoggerFactory.getLogger(this.getClass.getName.split("[.$]").last)

  logger.info("Starting the system ...")
  
  val config = ConfigFactory.load().resolve()

  val jobs = buildJobs(config)
  
  val system = PipeFlowSystem("MyApp", jobs, config)

  system.awaitTermination()

  logger.info("System finished ...")
  
  def buildJobs(config: Config): Seq[Job] = ???
}
```
