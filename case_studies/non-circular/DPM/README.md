# Dynamic Power Management 

### Description
Dynamic Power Management (DPM) is an automatic system available in multiple devices such as Network Interface Cards (NICs), DRAM and disk devices to implement different power management strategies, usually switching between power modes that have different tradeoffs between performance and energy consumption. 

In this scenario, we considered a device with a DPM consisting of four parts: a service requester (SR) with a service request queue (SRQ), a service provider (SP) and a power manager (PM).

<img src="https://github.com/user-attachments/assets/bc5b5464-69eb-4322-b6c2-b7cb09df43e3" style="width: 40%;">

The SR requests are added into the queue

```
// This is the Service Requester (SR) and the Service Request Queue (SRQ)
// models combined.
// This model has no dependencies and two environmental parameters:
//

//References: 
// https://inria.hal.science/inria-00458053/document
// https://www.prismmodelchecker.org/casestudies/power_ctmc3.php


//This is the 

ctmc

// size of queue
const int QMAX=20;

// rate of arrivals
const double request; //=100/72; 

// SERVICE REQUESTER AND SERVICE REQUEST QUEUE
module SRQ

	q : [0..QMAX]; //request queue states
	//State transition
	[request] true -> request : (q'=min(q+1,QMAX)); // request arrives
	[serve]  q>0 -> (q'=q-1); // request served
	
endmodule
```

### References
[1] PRISM repository of case studies: https://www.prismmodelchecker.org/casestudies/power_ctmc3.php
[2] Calinescu, Radu, and Marta Kwiatkowska. "Using quantitative analysis to implement autonomic IT systems." 2009 IEEE 31st International Conference on Software Engineering. IEEE, 2009.
[3] Qiu, Qinru, Qing Wu, and Massoud Pedram. "Stochastic modeling of a power-managed system: construction and optimization." Proceedings of the 1999 international symposium on Low power electronics and design. 1999.
