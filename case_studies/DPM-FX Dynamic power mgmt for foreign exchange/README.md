# Dynamic Power Management (DPM) for Foreign Exchange (FX) System Case Study

## Overview
This case study examines a **foreign exchange (FX) service-based system**[1] that automates financial transactions and relies on a **dynamic power management (DPM) component** [2,3] to optimize power consumption for its database operations.

## System Description

The **FX system** performs automated trading and market analysis, relying on a database for processing large amounts of financial data. 


<p align="center">
  <img src="https://github.com/user-attachments/assets/5d14ea78-ad25-4380-975b-0afd1edb8528" width="45%">
</p>


The **DPM component** manages the power consumption of the hard disk storing this database by switching it to a low-power **sleep mode** when idle.

<p align="center">
  <img src="https://github.com/user-attachments/assets/1471c26f-2b76-4593-8f6c-64f2a6c5afff" width="45%">
</p>


Key system behaviours:
- The **DPM component** transitions the hard disk to sleep mode based on a configurable probability (**pIdle2Sleep**).
- The **FX system's transaction volume** affects the number of database operations (**diskOps**) performed.
- The **DPM queue length** (**avrQueueDiskOps**) influences how efficiently transactions are processed.

## Probabilistic Modeling

The system is represented using two **co-dependent stochastic models**:
- **DTMC model (m_fx)** for FX operations.
- **CTMC model (m_dpm)** for the DPM component.

These models interact dynamically:
- **FX transactions** impact the number of disk operations handled by DPM.
- **DPM queue length** affects the FX system’s execution speed.

## Objectives

Using ULTIMATE, we analyze the following properties by varying **pIdle2Sleep**.
1. **FX Workflow Execution Time**: The expected number of transaction processing time in miliseconds.
2. **Power Consumption Trade-offs**: The effect of DPM decisions on performance and energy savings.


<p align="center">
  <img src="https://github.com/user-attachments/assets/8a60b4a4-3715-466c-a05d-8b18249a6e8e" width="30%">
</p>


## References

[1] Fang, X., Calinescu, R., Gerasimou, S., & Alhwikem, F. (2023). Fast Parametric Model Checking with Applications to Software Performability Analysis. IEEE Transactions on Software Engineering, 49(10), 4707-4730. (Model file for case study prismPROBModel-2_P1.pm downloadable from: https://www.cs.york.ac.uk/tasp/fPMC/cases.htm)

[2] PRISM repository of case studies: https://www.prismmodelchecker.org/casestudies/power_ctmc3.php

[3] Kwiatkowska, M., Norman, G., & Parker, D. (2007). Stochastic model checking. Formal Methods for Performance Evaluation: 7th International School on Formal Methods for the Design of Computer, Communication, and Software Systems, SFM 2007, Bertinoro, Italy, May 28-June 2, 2007. (page 40)

