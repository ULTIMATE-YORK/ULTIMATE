# Mobile Robot Fleet (RoboFleet) Case Study

## Overview

This case study examines a **human-supervised fleet of N mobile robots**, each performing an independent mission within a grid world. Robots must navigate to a goal location to complete a task while avoiding obstacles and managing potential failures.

## Robots Description

Each **robot** moves up, down, left, or right within a **bounded grid world**, avoiding locations with obstacles. The mission consists of:
- Starting from an **initial location**.
- Navigating to a **goal location**.
- Performing a task upon reaching the goal.

However, at each location, a robot may become **stuck** with a small probability, which depends on the distance to nearby obstacles.

## Human Supervisor Description

The **human supervisor** monitors the fleet and attempts to free blocked robots using two strategies:
1. **Remote Maneuvering**: A low-cost action with medium success probability, attempted up to **nAttempts** times.
2. **Hard Reset**: A high-cost action with a higher probability of success.

## Probabilistic Modelling

Each robot is modelled as a **Markov Decision Process (MDP)**:
- **m_r1, m_r2, …, m_rN** represent the N robot models.
- These models are used the **optimal movement policies** to reach a goal with a **maximum mission success probabilities (pR1, pR2, ..., pRN)**.

The **human supervisor** is modeled as a **Discrete-Time Markov Chain (DTMC)**:
- **m_sup** represents the supervisor's decision-making process.
- Reward structures allow verification of:
  - The **expected number of failed robots**.
  - The **supervisor-intervention cost** across all missions.

## Objectives

Using ULTIMATE, we analyze the following properties based on the number of task attemps.
1. **Mission Success Probability**: The likelihood of each robot completing its task.
2. **Supervisor Intervention Cost**: The total cost incurred in unsticking robots.

<img src="https://github.com/user-attachments/assets/58655cac-d322-45b6-afc4-b9b4a78d36ee" style="width: 60%;">
