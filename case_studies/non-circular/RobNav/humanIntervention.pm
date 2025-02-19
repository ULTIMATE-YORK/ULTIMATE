// Human intervention model

// Aim:
// This model is used to obtained the expected cost to be paid
// for a suppervisor in charge of fixing a series of robots
// everytime any gets stock in their activities.
// Rmax=?[F done]

// Model description:
// The model describes a supervisor, worker @w, intervining when an error is detected.
// The probability of error depends on the independent models of each robot.

dtmc

formula done = w=2; // task done at T1

//--prob. of failing with their activities, from each robot's model.
const double p_r1;
const double p_r2;

module supervisor1
 w:[0..2] init 0;
 [ ]  w=0 -> (p_r1+p_r2-p_r1*p_r2):(w'=1) + (1-((p_r1+p_r2-p_r1*p_r2))):(w'=2);
 [interveneAfterFail] w=1 -> 1:(w'=2);  // worker intervention
 [completed] w=2 -> 1:(w'=2);
endmodule


rewards "intervation_cost"
 w=1 : 100;
endrewards