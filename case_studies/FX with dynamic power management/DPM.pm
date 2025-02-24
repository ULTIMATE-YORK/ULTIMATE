// Model from PRISM case studies' repository
// https://www.prismmodelchecker.org/casestudies/power_ctmc3.php

// Modified:

// SERVICE PROVIDER
// assume SP automatically
// moves from idle to busy when ever a request arrives
// moves from busy to idle when ever a request is served

ctmc

// rates of local state changes
const double sleep2idle=10/16;
const double idle2sleep=100/67;
// rate of service
const double service=1000/1100;//1000/8; 

module SP

	sp : [0..2]; // 0 - sleep, 1 - idle,  2 - busy
	
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

// SERVICE REQUESTER AND SERVICE REQUEST QUEUE

// size of queue
const int QMAX=20;

// rate of arrivals
//const double nRequests = 85.08123943822898; //<---------- From FX model, props: R{"disk_operations"}=?[F "done"]
//const int timeForFX = 2; //<---- Also get from FX

const double disk_ops = 85.081; //<---- From FX
const int FX_execution_per_time_unit = 2; //<---Fixed
const double nRequests = disk_ops * FX_execution_per_time_unit;

const double request=1/nRequests; //sec. per operation


module SRQ

	q : [0..QMAX];
	
	[request] true -> request : (q'=min(q+1,QMAX)); // request arrives
	[serve]  q>0 -> (q'=q-1); // request served
	
endmodule


// POWER MANAGER performance constraint 1
// when the SRQ is full and the SP is in sleep go to idle
// when the SRQ is empty and the SP is in idle:
// go to sleep with probability 0.008963 and stay in idle with probability 0.991037

module PM

	p : [0..1];
	// p=0 - loop or go sleep to idle
	// p=1 - idle to sleep

	// queue is full so go from sleep to idle
	[sleep2idle] q=QMAX -> (p'=p);
	// probabilistic choice when queue becomes empty
	[serve] q=1 -> 0.008963 : (p'=1);
	[serve] q=1 -> 0.991037 : (p'=0);
	[serve] q>1 -> true; // loops for remaining states
	[request] true -> (p'=0); // idle to sleep
	[idle2sleep] p=1 -> (p'=0); // reset p when queue is no longer empty
	
endmodule


//Use the DPM model to compute the expected request 
//service time SvcTime as some 
// constant*average_number_of_requests_awaiting_service 
// (NB: the average_number_of_requests_awaiting_service is one 
// of the DPM model properties explored in the PRISM case study).

// added rewards (to be checked):
rewards "queue_size"
	true : q;
endrewards

rewards "time"
	true: 1; //I don;t think this is correct as not a DTMC but CTMC
endrewards
