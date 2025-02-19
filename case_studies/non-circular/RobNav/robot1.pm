// Robot 1 - model

// Aim:
// This model is intended to obtain a policy that maximises the probability 
// of succeeding with its tasks; then obtained the probability of failure
// of this policy. This is done by model checking:
// 1 - Pmax=?[F done]

// Model description:
// The model describes @robot1 moving accross a grid.
// Each location l1-l9 have a probability of transitioning 
// through that location (space), for example, the robot can pass
// through l3 with 0.9 probability as this is farer from obstacles.
// Here there is only one obstable at l7.
// Robot r1 is at l1 and has to do T1 at l9.

// |-----|-----|-----|  
// | l1  | l2  | l3  |
// | 1 r1| 0.9 | 0.9 |
// |-----|-----|-----| 
// | l4  | l5  | l6  |
// | 0.6 | 0.8 | 0.9 |
// |-----|-----|-----| 
// |/l7//|  l8 |  l9 |
// |/////| 0.6 | 1 T1|
// |-----|-----|-----| 

mdp 

formula done = l=9; // task done at T1
const int fail = 10; // robot failed to travel through an location area

//--prob. of success when travelling through a location lx
const double p_l1 = 1; // assume a probability of one of leaving the initial area
const double p_l2 = 0.9;
const double p_l3 = 0.9;
const double p_l4 = 0.6;
const double p_l5 = 0.8;
const double p_l6 = 0.9;
// l7 is blocked
const double p_l8 = 0.6;
const double p_l9 = 1; // assume a prob. of one of travelling in the goal location's area, l9
//--

module robot1
 l:[1..10] init 1;
 // from l1 
 [l1_down]  l=1 -> p_l4:(l'=4) + (1-p_l4):(l'=fail);
 [l1_right] l=1 -> p_l2:(l'=2) + (1-p_l2):(l'=fail);
 // from l2
 [l2_down]  l=2 -> p_l5:(l'=5) + (1-p_l5):(l'=fail);
 [l2_left]  l=2 -> p_l1:(l'=1) + (1-p_l1):(l'=fail);
 [l2_right] l=2 -> p_l3:(l'=3) + (1-p_l3):(l'=fail);
 // from l3
 [l3_down]  l=3 -> p_l6:(l'=6) + (1-p_l6):(l'=fail);
 [l3_left]  l=3 -> p_l2:(l'=2) + (1-p_l2):(l'=fail);
  // from l4
 [l4_up]  l=4 -> p_l1:(l'=1) + (1-p_l1):(l'=fail);
 [l4_right] l=4 -> p_l5:(l'=5) + (1-p_l5):(l'=fail);
 // from l5
 [l5_up]  l=5 -> p_l2:(l'=2) + (1-p_l2):(l'=fail);
 [l5_down]  l=5 -> p_l8:(l'=8) + (1-p_l8):(l'=fail);
 [l5_left]  l=5 -> p_l4:(l'=4) + (1-p_l4):(l'=fail);
 [l5_right] l=5 -> p_l6:(l'=6) + (1-p_l6):(l'=fail);
 // from l6
 [l6_up]  l=6 -> p_l3:(l'=3) + (1-p_l3):(l'=fail);
 [l6_down]  l=6 -> p_l9:(l'=9) + (1-p_l9):(l'=fail);
 [l6_left]  l=6 -> p_l5:(l'=5) + (1-p_l5):(l'=fail);
 // from l7 - Obstacle
 // from l8
 [l8_up]  l=8 -> p_l5:(l'=5) + (1-p_l5):(l'=fail);
 [l8_right] l=9 -> p_l9:(l'=9) + (1-p_l9):(l'=fail);

 // from l9 (goal)
 [goalReached]  l=9 -> 1:(l'=9);
 // from l10 (fail)
 [fail] l=10 -> 1:(l'=10);
endmodule