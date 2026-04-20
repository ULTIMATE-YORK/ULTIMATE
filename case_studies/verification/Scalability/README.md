# Scalability experiments

Project (details): verification time (on a MacBook Pro with M1 Pro chip and 32GB RAM)

small_0 (1 model with 283 states and 695 transitions; 0 dependency levels; 0 dependencies): 0.47 seconds  
small_1 (3 models with 283 states and 695 transitions each; 1 dependency level; 2 dependencies): 0.80 seconds  
small_2 (11 models with 283 states and 695 transitions each; 2 dependency level; 10 dependencies): 2.11 seconds  
small_3 (75 models with 283 states and 695 transitions each; 3 dependency level; 74 dependencies): TBD

med_0 (1 model with 21,801 states and 41,873 transitions; 0 dependency levels; 0 dependencies): 0.64 seconds  
med_1  (3 models with 21,801 states and 41,873 transitions each; 1 dependency level; 2 dependencies): 1.21 seconds  
med_2 (11 models with 21,801 states and 41,873 transitions each; 2 dependency level; 10 dependencies): 3.63 seconds  
med_3 (75 models with 21,801 states and 41,873 transitions each; 3 dependency level; 74 dependencies): TBD

large_0 (1 model with 529,239 states and 1,061,479 transitions; 0 dependency levels; 0 dependencies): 3.56 seconds  
large_1 (3 models with 529,239 states and 1,061,479 transitions each; 1 dependency level; 2 dependencies): 9.67 seconds  
large_2 (11 models with 529,239 states and 1,061,479 transitions each; 2 dependency level; 10 dependencies): 33.74 seconds
large_3 (75 models with 529,239 states and 1,061,479 transitions each; 3 dependency level; 74 dependencies): TBD
