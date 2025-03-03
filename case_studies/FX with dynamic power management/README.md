# Foreign exchange (FX) with dynamic power management (DPM)

## Description
This Ultimate world consists of two systems, a foreign exchange (FX) system [1]  and a dynamic power management (DPM) system [2,3]. 

<img src="https://github.com/user-attachments/assets/72d4d4a5-82f2-4df3-b50c-5783ef86e39a" style="width: 60%;">


## FX model

The FX system has two execution modes. In a expert mode, it uses a Market Watch to obtain real-time exchange rates and a Technical Analysis to evaluate trading conditions, future price movements, etc. In a normal mode, it utilises a Fundamental Analysis evaluating th economic outlook of a country. A representation of the probabilistic model is depicted below, where ```x,y1,y2,z1,z2``` represent system probabilities. The system is designed to avoid single-point failures by implementing two (functionally equivalent) services at each state.  

### Reward 1
The [FX DTMC model](https://github.com/ULTIMATE-YORK/WorldModel/blob/main/case_studies/FX%20with%20dynamic%20power%20management/FX.pm) has two reward structures. The first, for the expected number of disk operations, which depends on the number of operations required by the Technical and Fundamental analysis, and the  

The first for the number of expected disk operations when the system perform a Market Watch or a Technical Analysis operation. We assume that the services Technical Analysis and Fundamental Analysis are deployed and run on the same server, that their executions require, on average, 12 and 20 disk operations. 

```
// ---- Reward for number of expected disk operations---------
const int num_disk_operations_Technical_analysis = 12; // environmental var.
const int num_disk_operations_Fundamental_analysis = 20; // environmental var.
label "done" = state=10 | state=9; //10=completed succ. 9=failed
rewards "disk_operations"
        //expert (Technical-OP2=2) or normal model (Fund-OP4=4)
	state=OP2 : num_disk_operations_Technical_analysis/avr_num_disk_ops_remain_in_queue;
	state=OP4 : num_disk_operations_Fundamental_analysis/avr_num_disk_ops_remain_in_queue;
endrewards
```

The value of ```const double avr_num_disk_ops_remain_in_queue``` is obtained from the DPM model.

### Reward 2





## References

[1] Fang, X., Calinescu, R., Gerasimou, S., & Alhwikem, F. (2023). Fast Parametric Model Checking with Applications to Software Performability Analysis. IEEE Transactions on Software Engineering, 49(10), 4707-4730. (Model file for case study prismPROBModel-2_P1.pm downloadable from: https://www.cs.york.ac.uk/tasp/fPMC/cases.htm)

[2] PRISM repository of case studies: https://www.prismmodelchecker.org/casestudies/power_ctmc3.php

[3] Kwiatkowska, M., Norman, G., & Parker, D. (2007). Stochastic model checking. Formal Methods for Performance Evaluation: 7th International School on Formal Methods for the Design of Computer, Communication, and Software Systems, SFM 2007, Bertinoro, Italy, May 28-June 2, 2007. (page 40)
