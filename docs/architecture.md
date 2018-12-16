# Architecture

The main components at the application level are:
- **The user application**: it is built using the DSL/API, initialises the system, loads configuration,
   and contains the logic necessary to build and schedule tasks. 
- **The periodic scheduler**: it allows to build tasks periodically.
- **The task scheduler and dependency management**: takes care of managing the tasks state, their dependencies,
   and orchestrate their execution. It is also the responsible to coordinate execution between different applications.
- **The execution subsystem**: the tasks actions can be delegated to other systems for the execution.
   It can be something as simple as a subprocess, or something more elaborated as a Mesos cluster.
   The execution subsystem will be plugable to allow the implementation of different modules for different systems.
- **The external interface** (REST/UI): it will allow to track the tasks state and perform operations on the system.

