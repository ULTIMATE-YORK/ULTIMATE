# Foreign exchange (FX) with dynamic power management (DPM)

## Description
This Ultimate world consists of two systems, a foreign exchange (FX) system [1]  and a dynamic power management (DPM) system [2,3]. 

<img src="https://github.com/user-attachments/assets/72d4d4a5-82f2-4df3-b50c-5783ef86e39a" style="width: 60%;">


## FX model

The FX system has two execution modes. In an expert mode, it uses a Market Watch to obtain real-time exchange rates and a Technical Analysis to evaluate trading conditions, future price movements, etc. In a normal mode, it utilises a Fundamental Analysis evaluating the economic outlook of a country. A representation of the probabilistic model is depicted below, where ```x,y1,y2,z1,z2``` represent system probabilities. The system is designed to avoid single-point failures by implementing two (functionally equivalent) services at each state.  

<img src="https://github.com/user-attachments/assets/5d14ea78-ad25-4380-975b-0afd1edb8528" style="width: 70%;">



### Reward 1
The [FX DTMC model](https://github.com/ULTIMATE-YORK/WorldModel/blob/main/case_studies/FX%20with%20dynamic%20power%20management/FX.pm) has two reward structures. The first is for the number of expected disk operations when the system performs a Market Watch or a Technical Analysis operation. We assume that the services Technical Analysis and Fundamental Analysis are deployed and run on the same server and that their executions require, on average, 12 and 20 disk operations. 

This reward obtains the number of disk operations to be requested by the FX system. These disk operation requests are added to the queue of the DPM system model. The larger the size of the queue, the fewer requests from FX to avoid filling up the DPM queue. Hence, the number of operations to queue when the FX system performs a Technical Analysis is given by ```num_disk_operations_Technical_analysis/avr_num_disk_ops_remain_in_queue```.

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

Given that the same server runs a DPM, the value of ```avr_num_disk_ops_remain_in_queue``` is obtained from the verification DPM system's model.

| Parameter              | Value |
|-----------------------|-------------|
|avr_num_disk_ops_remain_in_queue| PMC( m_{DPM} , R{"queue_size"}=? [ S]) |

where ```m_{DPM}``` is the DPM model.


### Reward 2

A second reward obtained adds the time spent at each FX operation. The service time (SvcTime) for the Technical and Fundamental also depends on the value of ```avr_num_disk_ops_remain_in_queue``` as before, and a constant ```c``` representing the time required per disk operation.

```
// ----- Reward for time -----------
const double c = 1; //<---- time units per disk operation
const double SvcTime = c * avr_num_disk_ops_remain_in_queue;
const double time1 = 10;
const double time2 = SvcTime;
const double time3 = 10;
const double time4 = SvcTime;
const double time5 = 10;
const double time6 = 10;
rewards "time"
     state=OP1 : time1;
     state=OP2 : time2 *num_disk_operations_Technical_analysis; //technical analysis
     state=OP3 : time3;
     state=OP4 : time4 *num_disk_operations_Fundamental_analysis;//fund. analysis
     state=OP5 : time5;
     state=OP6 : time6;
endrewards
```





## DPM model

The [DPM CTMC](https://github.com/ULTIMATE-YORK/WorldModel/blob/main/case_studies/FX%20with%20dynamic%20power%20management/DPM.pm) models a power management system with four different components as shown in the following figure: PM, SR, SRQ and SP.

<img src="https://github.com/user-attachments/assets/1471c26f-2b76-4593-8f6c-64f2a6c5afff" style="width: 70%;">

## 



## References

[1] Fang, X., Calinescu, R., Gerasimou, S., & Alhwikem, F. (2023). Fast Parametric Model Checking with Applications to Software Performability Analysis. IEEE Transactions on Software Engineering, 49(10), 4707-4730. (Model file for case study prismPROBModel-2_P1.pm downloadable from: https://www.cs.york.ac.uk/tasp/fPMC/cases.htm)

[2] PRISM repository of case studies: https://www.prismmodelchecker.org/casestudies/power_ctmc3.php

[3] Kwiatkowska, M., Norman, G., & Parker, D. (2007). Stochastic model checking. Formal Methods for Performance Evaluation: 7th International School on Formal Methods for the Design of Computer, Communication, and Software Systems, SFM 2007, Bertinoro, Italy, May 28-June 2, 2007. (page 40)
