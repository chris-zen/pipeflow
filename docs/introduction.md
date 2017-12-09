# Introduction

PipeFlow is a task orchestration manager inspired by Luigi and AirFlow,
 that brings some of the ideas of how micro-services are developed and operated to this area.
 It is developed with Scala, and won't require any kind of central scheduler or server to operate,
 as every application (or set of workflows) will be fully autonomous and provide its own web interface.

It provides a DSL for the user to specify:
- the tasks that need to be run
- its dependencies
- the data consumed and produced
- how should them be scheduled
- organisational metadata

The DSL has been designed from the ground up taking into account the following ideas:
- Everything can be fully tested, and dependencies can be injected easily.
- The tasks are defined declaratively through code, using immutable data structures,
   that can be parameterised and composed following basic functional programming principles.
- It is very easy to experiment with new ideas by replacing or enhancing existing components.

It takes from AirFlow the idea of the user declaring the tasks and its dependencies through code,
 and complements it with the Luigi idea of declaring explicitly what outputs every task generates.
 Furthermore it also allows to declare inputs, and let the system infer implicit dependencies between tasks.
 Pipeflow also provides a way to organise tasks in groups hierarchically, allowing to organise them logically.
 The different parts of a workflow can be fully composable, re-usable, parametrised and immutable, by just using
 pure functional principles and the goodies from the Scala language.
 Every task defines an action, that represents the logic that needs to be executed for it to be completed.
 
PipeFlow has its own periodic scheduler, as well as an event system, that can be used to build and
 launch tasks periodically or reactively.
 It will manage the dependencies, the order of execution, and when all the requisites are ready,
 and will delegate the execution of the action to the execution subsystem that has been configured
 (local processes, a mesos framework, ...).

It will provide a REST interface as well as a web UI to help tracking the state.
