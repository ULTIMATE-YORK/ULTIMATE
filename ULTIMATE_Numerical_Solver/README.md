# Command Examples

### RAD - Prism invocation
     
    python3 ULTIMATE_numerical_solver.py \
    --path "/Users/simos/Documents/Software/prism-4.8-mac64-arm/bin/prism" \
    --mc "Prism" \
    --model "select_perception_model.dtmc" \
    --input "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect, pNotOkCorrect" "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect, pOkCorrect" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1, pModel2" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2, pModel1"


### RAD - Storm invocation
    python3 ULTIMATE_numerical_solver.py \
    --path "storm" \
    --mc "Storm" \
    --model "select_perception_model.dtmc" \
    --input "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect, pNotOkCorrect" "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect, pOkCorrect" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1, pModel2" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2, pModel1"


### Command Line Arguments
    
    --path: the path to Prism/Storm 
    --mc:  the selected probabilistic model checker
    --model: the model within the SCC to start the optimisation from
    --input: the set of dependency parameters within SCC models
    

Each input has the structure:
     
1) dependent_model
2) source_model
3) property
4) variable names in dependent model starting with the dependent variable for the dependent model, e.g.,
<br/>    

     select_perception_model.dtmc, perceive-user2 dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect, pNotOkCorrect
