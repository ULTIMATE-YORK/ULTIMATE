// This model represents a robot perception system consisting of multiple cameras for the detection of 
// obtacles around the robot. This is used for collision avoidance, as it detects obstacles on the front
// and on the sides of the robot, so it can decide if continuing ahead or moving to one of the sides.

// This system is used later for a mobile robot to decide if it should move forward or
// diagonally. If the perception system stops working, the robot's actions
// can on previous probabilisitc information: "What is the probability of an obstacle ahead in the next N steps"

dtmc

label "obstacle_ahead" = collision=1;

// environmental parameter (fixed):
// day =1 - working on day light
// day =0 - working on artificial light
const int day = 1;

// internal params.:
// with light prob. of obstacles
const double p_obs_ahead = 0.1;
const double p_obs_right = 0.05;
const double p_obs_left = 0.05;

const double p_obs_side = p_obs_right + p_obs_left;
const double p_no_obs = 1-p_obs_ahead-p_obs_side;

// with no light prob. of obstacles
const double p2_obs_ahead = 0.2;
const double p2_obs_right = 0.2;
const double p2_obs_left = 0.2;

const double p2_obs_side = p2_obs_right + p2_obs_left;
const double p2_no_obs = 1-p2_obs_ahead-p2_obs_side;


module lightSystem
	//collision=1 -- collision due to obtacle ahead
	//collision=2 -- collision due to obtacle on the side

	collision : [0..2];
	// with light
	[] day = 1 -> p_obs_ahead: (collision'=1) + p_obs_side: (collision'=2) + p_no_obs: (collision'=0);
	// without light
	[] day = 0 -> p_obs_ahead: (collision'=1) + p_obs_side: (collision'=2) + p_no_obs: (collision'=0);
endmodule




