# Dynamic Power Management 

## Description
Dynamic Power Management (DPM) is an automatic system available in multiple devices such as Network Interface Cards (NICs), DRAM and disk devices to implement different power management strategies, usually switching between power modes that have different tradeoffs between performance and energy consumption. 

In this scenario, we considered a device with a DPM consisting of four parts: a service requester (SR) with a service request queue (SRQ), a service provider (SP) and a power manager (PM).

<img src="https://github.com/user-attachments/assets/bc5b5464-69eb-4322-b6c2-b7cb09df43e3" style="width: 40%;">

## Models
### SR and SRQ
The SR requests are added into the queue

```
// This is the Service Requester (SR) and the Service Request Queue (SRQ)
// models combined.
// This model has no dependencies and two environmental parameters:
//

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

## SP
```
// This is the Service Provider (SP) model of a Power Distribution Management.
// The SP model depends on the probability of having no requests in the Service
// Request Queue (SRQ). Hence, the dependency param. is: 
//   d^{sp}_1 = p_Q0     //probability of no requests queued (used as a rate)
// There are external parameters:
//   e^{sp}_1 = sleep2idle  //transition rate from sleep to idle
//   e^{sp}_2 = idle2sleep  //transition rate from idle to sleep
//   e^{sp}_3 = service     //rate from sleep to idle
// Internal parameters
//   x^{sp}_1 = p_Qn        
// 
// Dependency and external params. are define with dummy values.


ctmc

// rates of local state changes
const double sleep2idle=10/16; 
const double idle2sleep=100/67;
// rate of service
const double service=1000/8;  //service rate

const double p_Q0 = 1/10; //prob. of no requests queued
const double p_Qn = 1-p_Q0; //prob. of queued more than one request

// SERVICE PROVIDER
module SP
// assume SP automatically:
// -moves from idle to busy whenever a request arrives
// -moves from busy to idle whenever a request is served

	sp : [0..2]; // 0 - sleep, 1 - idle,  2 - busy
	q: [0..10];

	[] true -> p_Q0:(q'=0) + p_Qn:(q'=1); //rates of transition based on prob. of requests in queue
	
	// SLEEP TO IDLE
	// when there is nothing in the queue go to idle
	[sleep2idle] sp=0 & q=0 -> sleep2idle : (sp'=1);
	// when there is something in the queue so start serving immediately
	[sleep2idle] sp=0 & q>0 -> sleep2idle : (sp'=2);

	// IDLE TO SLEEP
	[idle2sleep] sp=1 & q=0 -> idle2sleep : (sp'=0);

	// IDLE TO BUSY (when a request arrives)
	[request] sp=1  -> (sp'=2);
	[request] !sp=1 -> true; // need to add loop for other states
	
	// SERVE REQUESTS
	[serve] sp=2 & q>1 -> service : (sp'=2); 
	[serve] sp=2 & q=1 -> service : (sp'=1); 
endmodule
```



### References
[1] PRISM repository of case studies: https://www.prismmodelchecker.org/casestudies/power_ctmc3.php
[2] Calinescu, Radu, and Marta Kwiatkowska. "Using quantitative analysis to implement autonomic IT systems." 2009 IEEE 31st International Conference on Software Engineering. IEEE, 2009. https://inria.hal.science/inria-00458053/document
[3] Qiu, Qinru, Qing Wu, and Massoud Pedram. "Stochastic modeling of a power-managed system: construction and optimization." Proceedings of the 1999 international symposium on Low power electronics and design. 1999.
