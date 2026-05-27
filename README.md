# Distributed Task Scheduler

A Java Swing simulation of dynamic task scheduling across heterogeneous servers using priority-aware scoring.

## Overview

This project simulates a distributed computing environment where computational tasks are assigned to multiple servers.

The main objective is to implement an intelligent scheduler capable of selecting the most suitable server for each task based on:

- server computing power
- current server load
- task priority
- estimated execution time

Instead of using a basic strategy such as round-robin or always selecting the fastest server, the scheduler uses a score-based decision system to balance performance and load.

## Technologies Used

- Java
- Java Swing
- Object-Oriented Programming


## Project Architecture

The project is organized around a clear separation of responsibilities.

| Class | Responsibility |
|---|---|
| `Task.java` | Represents a computational task with an ID, size, and priority |
| `Server.java` | Represents a server with computing power, load, and score calculation methods |
| `Scheduler.java` | Contains the scheduling logic and selects the best server for each task |
| `SimulationStats.java` | Collects and displays simulation metrics |
| `GridSim.java` | Main entry point and graphical interface |

## Scheduling Strategy

The scheduler evaluates each server before assigning a task.

- `NORMAL` tasks use a power/load compromise.
- `URGENT` tasks prioritize raw computing power.

### Normal Tasks

For normal-priority tasks, the scheduler uses the following formula:

```text
score = power / (1 + load)
```

### Urgent Tasks

```text
urgent_score = power × 2
```

## Interface

The application includes a Java Swing interface for managing servers, tasks, and scheduling results.

The interface is divided into three main sections:

- **Servers**: displays server power, load, normal score, and urgent score.
- **Tasks**: displays task size and priority.
- **Decisions**: shows the selected server for each task after running the simulation.
  
It also includes summary cards for total tasks, normal tasks, urgent tasks, and average execution time.

<p align="center">
  <img src="https://github.com/user-attachments/assets/169a90b6-d27a-48b1-acc1-51f6937efef4" alt="Java Distributed Task Scheduler Interface" width="700">
</p>
