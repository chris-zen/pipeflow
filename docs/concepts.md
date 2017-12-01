# Basic Concepts

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

## Workflow

A `Workflow` is a set of tasks organised in groups with dependencies to either data or other tasks or groups.

There is no explicit representation for it, as either a `Task` or a `Group` can represent a workflow.
 In fact both of them implement the trait `Node` that can be used in different places.
 
In the previous example, the `preprocessing` node can be considered a workflow.


## Requirements

Tasks and groups (nodes) can depend on each other's completion before its execution can start, but also on other kind of conditions,
 such as the existence of external data sources, some event to happen, and so on and so forth.
 The DSL defines the trait `Requierement` to represent such node requirement for execution.
 And it also provides several specialisations, such as the `NodeRequirement` that represents
 a requirement on another node to complete, or the `DataRefRequirement` that represents a requirement on a data source to exist.
 In fact, the `Task` provides a convenience method to define requirements on input data (`input`),
 a part from the standard one to specify requirements (`requires`).

The following exmples shows how such requirements can be defined in different ways:

```scala
import pipeflow.dsl.nodes.Task

// task1 needs to execute and complete before task2 can start
val task1 = Task("task1")
val task2 = Task("task2").requires(NodeRequirement(task1))


// the dependency can be simplified importing an implicit conversion
import pipeflow.dsl.requirements.NodeRequirement._
val task3 = Task("task3").requires(task1)
```

## TODO

- Action
- Parameters
- Application
