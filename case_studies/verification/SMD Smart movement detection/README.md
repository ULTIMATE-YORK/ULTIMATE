# Smart Motion Detection (SMD) Case Study

## Overview

This case study examines a cyber-physical system that integrates two co-dependent components:
1. A **night-time motion sensor** that detects movement and triggers an alarm.
2. A **smart lighting component** that adjusts lighting levels based on sensor readings.

## Motion Sensor Description

The **motion sensor** operates at regular intervals, checking for movement and triggering an alarm based on detection probability. This probability is influenced by two key factors:
- **Scenario**: The monitored area may contain:
  1. A **large, relevant moving object** (e.g., intruder, cat, dog).
  2. A **small, irrelevant object** (e.g., wind-blown leaves).
  3. No moving objects.
- **Lighting Levels**: The probability of detection varies with lighting conditions, which can be:
  - Low (**pLow**)
  - Medium (**pMed**)
  - High (**pHigh = 1 - pLow - pMed**)

## Smart Lighting Component

The **smart lighting component** adjusts lighting levels every **30 seconds** based on:
1. The current lighting level.
2. The motion sensor's detection output.

## Probabilistic Modelling

Each component is modeled as a **Discrete-Time Markov Chain (DTMC)**:
- **m_ms** for the motion sensor.
- **m_sl** for the smart lighting.

These models are combined in the **ULTIMATE multi-model stochastic system**.

## Objectives and Results

Using ULTIMATE, we analyze the following properties based on different values of the probability of a large moving object in the monitored area pLargeObject.
1. **Detection Probability**: The likelihood of detecting a large object.
2. **Power Consumption**: Energy usage of the smart lighting component.

<p align="center">
  <img src="https://github.com/user-attachments/assets/09db5f5d-da84-4ea6-a97e-64dd391a9a74" width="30%">
</p>
