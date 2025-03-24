# Robot Assistive Dressing (RAD) System Case Study

## Overview

This case study examines a **robot assistive dressing (RAD) system**, a cyber-physical system designed to help individuals with restricted mobility dress independently using a robotic arm. This cyber-physical system belongs to a domain of growing societal importance due to the significant increase in demand for assistive care driven by an ageing population worldwide.

## System Description

The RAD system comprises multiple components working together to complete the dressing procedure while adapting to the user's state. Key components include:

1. **Garment Picking**: The robotic arm picks the garment from a nearby peg.
2. **User Monitoring**: A deep-learning perception system classifies the user’s state as **ok** or **notok**.
3. **User Monitor Controller**: A control component configures the user monitoring process.
4. **Dressing Process**: The robot assists the user, adapting its actions based on user feedback and successful garment picking.

## Probabilistic Modelling

The RAD system is modelled using four **interdependent stochastic models** of different types:

- **Garment Picking (CTMC)**: Models the robot's attempts to pick the garment within a 90s time constraint. Success probability (**pSucc**) and retry probability (**pRetry**) depend on external environmental factors like peg position and lighting.
- **User Monitoring (DTMC)**: This RAD component uses machine learning (ML) perception to classify the user as content with the ongoing dressing (i.e., ok) or not (notok). Comprises two machine learning models and a verifier:
  - **ML Model 1**: Medium accuracy, fast processing.
  - **ML Model 2**: High accuracy, computationally expensive.
  - **Verifier**: Determines if Model 1’s output is likely correct, switching to Model 2 when necessary.
- **User Monitor Controller (DTMC)**: Configures user monitoring based on **F1 score** and determines probabilities **pModel1** and **pModel2** for classifier selection.
- **Dressing Process (POMDP)**: Controls the robot’s dressing procedure, adapting to real-time user state classification.

## Objectives

Using **Probabilistic Model Checking (PMC)**, we aim to synthesize an **optimal policy** that minimizes the probability of dressing failure. Experimental results shows the probability of failure for different values of tue probability of user being ok (pOk) and retry probability (pRetry).


<p align="center">
  <img src="https://github.com/user-attachments/assets/0d208e30-2aa4-4e78-9dba-dba97fe4cc28" width="30%">
</p>
