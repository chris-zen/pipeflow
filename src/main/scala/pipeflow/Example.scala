package pipeflow

import java.net.URI

import pipeflow.core._

case class SparkJob(
  run: () => Unit,
  properties: Map[String, String] = Map.empty
) extends Action


object Example {
  /**
    * g2
    * +- g1
    * |  +- t1_1
    * |  +- t1_2
    * +- t2_1
    *
    * w2 [ w1 [ t1_1 -> t1_2 ] -> t2_1 ]
    *
    */

  val g1 = buildG1(2)

  val t2_1 = Task("t2_1")
    .name("name2")
    .input(DataRef.Uri(new URI("s3://y/in")))
    .output(DataRef.Uri(new URI("s3://y/out/year=2016/day=6")))
    .requires(g1)

  val g2 = Group("w2")
    .withGroup(g1)
    .withTask(t2_1)

//  val system = PipeflowSystem()
//  system.onEvent() { event =>
//    buildDAG(event)
//  }
//  system.scheduleHourly { time =>
//    w2
//  }

  def buildG1(numTasks: Int): Group = {
    val tasks = for (i <- 1 to numTasks)
      yield Task(s"t1_$i")
        .name(s"task $i")
        .input(DataRef.Uri(new URI(s"s3://x/in1/$i")))
        .output(DataRef.Uri(new URI(s"s3://x/out1/$i")))
        .action(SparkJob { () =>
          println(i)
        })

    Group("w1")
      .withTasks(tasks)
  }
}
