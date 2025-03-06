# Command Examples

### RAD - Prism invocation
     
     python3 Models/RAD/ULTIMATE_numerical_solver.py \
     --pmc "/Users/simos/Documents/Software/prism-4.8-mac64-arm/bin/prism" \
     --model "Models/RAD/select_perception_model.dtmc" \
     --input "Models/RAD/select_perception_model.dtmc, Models/RAD/perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect" "Models/RAD/select_perception_model.dtmc, Models/RAD/perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect" "Models/RAD/perceive-user2.dtmc, Models/RAD/select_perception_model.dtmc, P=?[F s=1], pModel1" "Models/RAD/perceive-user2.dtmc, Models/RAD/select_perception_model.dtmc, P=?[F s=2], pModel2"

### Command Line Arguments

    --pmc:  the path to prism
    --model: the model within the SCC to start the optimisation from
    --input: the set of dependency parameters within SCC models

Each input has the structure:
     
     "dependent_model, source_model, property, variable name in dependent model"