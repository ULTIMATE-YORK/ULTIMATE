# Scalability experiments (synthetic SWM)

This experiment evaluates scalability on benchmark models from the PRISM benchmark suite, assembled into a stochastic world model (SWM) with cross-model dependencies.

## Models and setup

We combined three of the largest publicly available PRISM benchmark models:

- **crowds**  
  - Parameters: `TotalRuns = 6`, `CrowdSize = 20`  
  - Type: DTMC  
  - Size: **10,633,591 states**
  - Dependencies: Two probabilities are parametrised. These parameters depend on reachability properties of **wlan6** and **cluster**.

- **cluster**  
  - Parameter: `N = 512`  
  - Type: CTMC  
  - Size: **9,465,876 states**

- **wlan6**  
  - Type: MDP  
  - Size: **5,007,552 states**

Overall, the composed SWM consists of **three large models** with heterogeneous semantics (DTMC, CTMC, and MDP).

This setup yields a **single dependency level** where the top-level `crowds` model depends on properties verified in the other two models.

## Verification results

- **Verified property:** top-level reachability property in `crowds`
- **Total verification time:** **324 seconds**
- **Time spent in PRISM invocations:** **319 seconds (98.4%)** (model building and verification subtasks).

Experiments were executed on a **Ubuntu 25.04 virtual machine** with 2 vCPUS and 8GB RAM, running within Oracle VM VirtualBox 7.1.12 deployed on a MacBook Pro with 2GHz Quad-Core i5 and 32GB RAM.


These results demonstrate that the ULTIMATE tool scales to tens of millions of states across multiple interacting models. Nearly all computation time is spent in the back-end model checker (PRISM in our implementation).
