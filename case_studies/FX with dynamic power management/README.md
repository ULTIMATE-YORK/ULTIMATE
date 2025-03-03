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

| Dependency parameter              | Value |
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

This "time" reward is used in the following property.

### FX verification properties
The property to verify for the FX model is the following.


| Property              | Description |
|-----------------------|-------------|
| R{"time"}=?[F "done"]<=100 | The expected time to complete the FX workflow is less than or equal to 100 time units. |




## DPM model

The [DPM CTMC](https://github.com/ULTIMATE-YORK/WorldModel/blob/main/case_studies/FX%20with%20dynamic%20power%20management/DPM.pm) models a dynamic power management system to save energy in devices that can be turn on and off. Examples of DPM schemes are ACPI and OnNow. The DPM model consists of four different components as shown in the following figure. A **Service Provider (SP)**, representing the device under power management control; a **Service Requester (SR)**, responsible for sending requests to the device; a **Service Request Queue (SRQ)**, which holds pending requests awaiting service; and a **Power Manager (PM)**, which monitors the system and issues commands to the SP based on a stochastic Dynamic Power Management (DPM) policy.

<!--There are three PM power states with different power consumption rates: sleep (when the SP is inactive and no requests are served), idle (when the SP is active but not working on any requests) and busy (where the SP is active and serving requests). Transitions between sleep and idle are controlled by the PM. Transitions between idle and busy are controlled by the SRQ's state. -->

<img src="https://github.com/user-attachments/assets/1471c26f-2b76-4593-8f6c-64f2a6c5afff" style="width: 70%;">

The SRQ and SR are modelled together as shown in the module below. Its rates of transitions depend on the arrival of a request, which in turn depends on the verification of the FX model. 


```
// SERVICE REQUESTER AND SERVICE REQUEST QUEUE
// average disk operations required for FX system
const double disk_ops; //<---------- From FX model, props: R{"disk_operations"}=?[F "done"]
const int FX_execution_per_time_unit = 2; // num. of FX total executions per time unit
const double nRequests = disk_ops * FX_execution_per_time_unit; // avr. number of disk op. requests per time unit
const double request=1/nRequests; //rate of requests (time units per operation)
const int QMAX=20; // size of queue
module SRQ
	q : [0..QMAX];
	[request] true -> request : (q'=min(q+1,QMAX)); // request arrives
	[serve]  q>0 -> (q'=q-1); // request served
endmodule
```

Hence, the disk_ops parameter value is computed as follows.

| Dependency parameter              | Value |
|-----------------------|-------------|
|disk_ops| PMC( m_{FX} , R{"disk_operations"}=?[F "done"]) |

where ```m_{FX}``` is the FX model.



### DPM verification properties
The properties to verify for the DPM model are the following.



| Property              | Description |
|-----------------------|-------------|
|R{"power"}=?[C<=referenceTimeInterval] | The expected average queue lenght duiring the first  _referenceTimeInterval_ seconds of operation.|


| P=?[F<=t (q > M)]     | The probability that the queue size becomes greater than or equal to M by time t. |
| P=?[F<=t (lost > M)]  | The probability that at least M requests get lost by time t. |
| R=?[C<=t]            | The expected power consumption by time t or the expected number of lost customers by time t (depending on whether the first or third reward structure is used). |
| R{"time"}=?[I=t]          | The expected queue size at time t (using the second reward structure). |
| R=?[S]              | The long-run average power consumption or long-run average queue size (depending on which reward structure is used). |
| R=?[S]              | The long-run average power consumption or long-run average queue size (depending on which reward structure is used). |






## References

[1] Fang, X., Calinescu, R., Gerasimou, S., & Alhwikem, F. (2023). Fast Parametric Model Checking with Applications to Software Performability Analysis. IEEE Transactions on Software Engineering, 49(10), 4707-4730. (Model file for case study prismPROBModel-2_P1.pm downloadable from: https://www.cs.york.ac.uk/tasp/fPMC/cases.htm)

[2] PRISM repository of case studies: https://www.prismmodelchecker.org/casestudies/power_ctmc3.php

[3] Kwiatkowska, M., Norman, G., & Parker, D. (2007). Stochastic model checking. Formal Methods for Performance Evaluation: 7th International School on Formal Methods for the Design of Computer, Communication, and Software Systems, SFM 2007, Bertinoro, Italy, May 28-June 2, 2007. (page 40)

[4] Calinescu, Radu, and Marta Kwiatkowska. "Using quantitative analysis to implement autonomic IT systems." 2009 IEEE 31st International Conference on Software Engineering. IEEE, 2009.
