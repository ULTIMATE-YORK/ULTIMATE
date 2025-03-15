# Command Examples

### RAD - Prism invocation
     
    python3 ULTIMATE_numerical_solver.py \
    --path "path to prism" \
    --mc "Prism" \
    --model "select_perception_model.dtmc" \
    --input "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect" "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2"


### RAD - Storm invocation (runs much faster than Prism)
    python3 ULTIMATE_numerical_solver.py \
    --path "storm" \
    --mc "Storm" \
    --model "select_perception_model.dtmc" \
    --input "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect" "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2"

## Result
    Optimal parameters: {'pModel1': 0.39958, 'pModel2': 0.30078, 'pOkCorrect': 0.41784, 'pNotOkCorrect': 0.42122}
Minimum objective value: 0.0031533

---
---

### SMD - Storm invocation (runs much faster than Prism)
     python3 ULTIMATE_numerical_solver.py \
     --path "storm" \
      --mc "Storm" \
      --model "SmartLighting.prism" \
      --input "SmartLighting.prism, MotionSensor.prism, P=?[F (step=2 & detected)], pDetected" "MotionSensor.prism, SmartLighting.prism, R{\"low\"}=? [C<=10000], pLow" "MotionSensor.prism, SmartLighting.prism, R{\"medium\"}=? [C<=10000], pMed" 

## Result
    Optimal parameters: {'pLow': 0.52249, 'pMed': 0.20032, 'pDetected': 0.27743}
    Minimum objective value: 2.230344e-05



### Command Line Arguments
    
    --path: the path to Prism/Storm 
    --mc:  the selected probabilistic model checker
    --model: the model within the SCC to start the optimisation from
    --input: the set of dependency parameters within SCC models
    

Each input has the structure:
     
1) dependent_model
2) source_model
3) property
4) variable name in dependent model
