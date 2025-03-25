# Robot Coordination (RoCo) Case Study

## Overview

This case study explores the **Robot Coordination (RoCo)** problem, focusing on the coordination of two robots tasked with navigating a **square grid world** while avoiding collisions [1]. The robots start at diagonally opposite corners of the grid and need to reach the opposite corners. The system accounts for uncertainty in their movements due to obstacles detected by their cameras, making the coordination process more complex. We model this scenario using a **concurrent stochastic game** and verify the robots' ability to reach their destinations safely, while considering the impact of obstacles and the accuracy of obstacle detection methods.

## System Description

The RoCo system involves two robots operating in a shared grid world, and key components of the system include:

1. **Grid World Navigation**: Each robot starts in one of the diagonally opposite corners of the grid and needs to move to the opposite corner. The robots can move to neighboring grid cells, including diagonally.
2. **Movement Uncertainty**: The robots face uncertainty in their movement. For example, if a robot plans to move south, there is a probability \( q \) that the robot will move in a direction adjacent to the planned one (e.g., south-east or south-west). This is due to obstacles detected by the robot's cameras.
3. **Obstacle Detection**: Each robot has a camera-based obstacle detection system that determines whether the next location is blocked. The obstacle-detection system workflow of each model involves several stages in which increasingly accurate but more energy consuming and computationally intensive methods are used to conservatively determine whether the location that the robot plans to move to next contains an obstacle.

## Probabilistic Modelling

The RoCo system is modeled using two primary components: 

- **Concurrent Stochastic Game (m_rob)**: The robots' coordination is modeled as a concurrent stochastic game \( m_\mathit{rob} \), where both robots move simultaneously across the grid. The game’s objective is to navigate the grid while avoiding collisions. For each move, there is a small probability \( q \) that the robot will move in a direction adjacent to the planned one due to detected obstacles. For example, if a robot intends to move south, it will actually move south-east or south-west with a probability of \( q/2 \), and south with a probability of \( 1 - q \).

- **Obstacle Detection Workflow (DTMC)**: Each robot's obstacle detection workflow is modeled as a **DTMC** \( m_\mathit{od} \), which simulates the process of detecting obstacles before a move. This model accounts for the robot's ability to detect obstacles with increasing accuracy but at the cost of higher energy consumption and computational load. The detection accuracy directly impacts the **dependency parameter \( q \)**, which influences the robot's movement behavior.


## Objectives

The primary objective of this case study is to assess how the robots’ coordination perform under varying levels of obstacle presence and detection accuracy. Specifically, we aim to verify how the combined probability of success (**pSucc1 + pSucc2**) for both robots to reach their destinations within **k** moves on a **7x7 grid** is affected by the actual probability of a grid cell being occupied by an obstacle (**pObstacle**).

<p align="center">
  <img src="https://github.com/user-attachments/assets/10d9163e-124a-4831-ad51-68df96ae5d8a" width="30%">
</p>


## References

[1] Case study in PRISM: http://www.prismmodelchecker.org/casestudies/robot_coordination.php
