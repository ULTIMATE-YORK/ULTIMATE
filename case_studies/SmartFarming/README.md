# Smart Farming

## Overview
This case study examines a smart farming system designed for vineyard operations, involving multiple autonomous mobile robots and a human supervisor. 
Due to the increasing adoption of precision agriculture and automation, such cyber-physical systems are becoming critical for scalable, sustainable farming. 
The system coordinates task execution such as vine monitoring while ensuring safety (collision avoidance) and balancing trade-offs between efficiency, accuracy, and energy use.


## System Description

The smart farming system involves two mobile robots and a human supervisor operating in a vineyard environment.

1. **Collision detection system (high-risk)**: High accuracy, computationally expensive obstacle collision detection system.
2. **Collision detection system (low-risk)**: Medium accuracy, fast processing  obstacle collision detection system.
3. **Robot1**: A mobile robot working in a vineyard (taking images of the vines at an specific location). The robot uses either a high- or low-risk collision detection system depending on how close it is to known static obstacles. The robot must select a path to reach the location of the next task.
5. **Robot2**: Similar to robot1, this second robot must collect vine images on different sections of the vineyard.
7. **Multi-robot multi-human (MR-MH) planning problem**: The smart farming problem involves coordinating two robots and a human supervisor in performing vineyard tasks, while managing task retries to balance mission success with operational costs.

**Problem:** Since both robots and the human worker may fail their assigned tasks, each task can be retried a limited number of times. Retries increase the likelihood of eventual task completion, but also incur additional energy costs. The challenge is to determine the trade-off between maximising the probability of mission success and minimising the expected overall mission cost.

## Probabilistic Modelling

The smart farming system is modelled using the following interdependent stochastic models:


1. **Collision detection models (DTMC)**: These models provide detection probabilities used by the robots.
   - Low-Risk Sensor: Models the probability of detecting collisions with medium accuracy and fast response.
   - High-Risk Sensor: Models collision detection with high accuracy but higher computational cost.
2. **Robot models (MDP)**: The robots' operate in a grid world environment that contains an obstacle at a given location. Each movement (up, down, left, right) succeeds with a location-dependent probability.
Locations near the obstacle have a higher risk of collision, requiring the high-risk (costly) collision detection sensor. Other locations rely on the low-risk sensor.
Both robots must move across locations to reach a final one, where they must collect vine images.
   - Robot1 starts at location l9 and aiming to reach location l2.
   - Robot2 starts at location l1 and aiming to reach location l8.
A policy of the MDP represents a path to the goal location.
3. **Multi-robot multi-human planning model (DTMC)**: The multi-robot multi-human (MRMH) system coordinates the human worker and both robots to complete the vineyard tasks.
Each agent may fail a task but has a limited number of retries, which increases the probability of eventual success at the expense of extra energy costs. 
Travel success probabilities for Robot1 and Robot2 depend on the policies derived from their respective MDP models.
Rewards (costs) are assigned for task execution and retries (e.g., Worker1 incurs higher cost per retry than robots).


## Synthesis Objectives

Using **parametric Probabilistic Model Checking (pPMC)**, we aim to synthesize the **number of possible retries for each failed task** that maximises the probability of mission success while minimising the mission cost.

Both conflicting objectives are defined within the .ultimate file as:

Mission success probability:
```
"Objective, max: P=?[F worker1=worker1Final & r1=r1Final & r2=r2Final]",
```
Expected mission cost:
```
"Objective, min: R=?[F ((worker1=worker1Final | worker1=worker1Fail) & (r1=r1Final | r1=r1Fail) & (r2=r2Final | r2=r2Fail))]"
```         

<!-- <p align="center">
  <img src="ADD FIGURE LINK https://github.com/XXX" width="30%">
</p> -->

