import numpy as np
from scipy.optimize import minimize
from collections import defaultdict
import argparse
import subprocess
import re
from enum import Enum


class ULTIMATE_Solver:
    def __init__(self, dependencies_list, pmc, path, ultimate_solver_enum):

        #dictionary to keep the dependency params
        #e.g., {'x': ULTIMATE_Dependency(x)}
        self.dependencies = defaultdict(list)
        
        #dictionary to keep the dependency parameters per model
        #e.g., {'m1': [ULTIMATE_Dependency(x), ULTIMATE_Dependency(y)]}
        self.model_dependencies = defaultdict(list)
        
        #dictionary to keep the dependent ULTIMATE_Dependency instances per param
        #e.g., {'x': ULTIMATE_Dependency('y')}
        self.param_dependencies = defaultdict(list)

        self.evaluated_params = {}


        for dep in dependencies_list:
            if ultimate_solver_enum is ULTIMATE_Solver_Enum.Numerical:
                dependency = ULTIMATE_Dependency(dep[0], dep[1], dep[2], dep[3], dep[4], pmc, path)
            else:
                dependency = ULTIMATE_Dependency_Formula(dep[0], dep[1], dep[2], dep[3], dep[4])
            
            #update the dependencies dictionary
            self.dependencies[dep[0]].append(dependency)
            
            #create the model_dependencies dictionary
            self.model_dependencies[dep[1]].append(dependency)

            #create the param_dependencies dictionary
            # for param in dep[3]:
            self.param_dependencies[dep[3]].append(dependency)



    def optimise(self, model_order):
        #find the number of parameters for model based on model_order[0]
        init_model = model_order[0]
        optimisation_parameters = []
        for dep in self.dependencies[init_model]:
            optimisation_parameters.append(dep.getVariableName())
        
        #get unique
        optimisation_parameters = list(set(optimisation_parameters))
        # print(optimisation_parameters)
        
        # Set the initial guess for the parameters
        x0 = np.array([0.1] * len(optimisation_parameters))

        # Define the bounds for the parameters (optional)
        bounds = [(0.00001, 1) for i in range(len(optimisation_parameters))]

        # Use the different algorithms to minimise the objective function and log the progress
        result = minimize(self.objective_Eval, x0, args=(optimisation_parameters, init_model, self.param_dependencies), method='Powell', bounds=bounds, tol=1e-3) #callback=callback_pModel)
        # 'L-BFGS-B'
        # return results and minimum loss
        return self.evaluated_params, result.fun



    def objective_Eval(self, x0, optimisation_parameters, init_model, param_dependencies):
        #create optimisation_parameter:value dictionary
        values = {k:v for k,v in zip(optimisation_parameters,x0)}

        #empty the dictionary
        self.evaluated_params.clear()

        for m in self.model_dependencies[init_model]:
            if m.getVariableName() not in self.evaluated_params.keys():
                evaluated_param_value = m.eval(values, param_dependencies)
            if isinstance(evaluated_param_value, np.float64):
                self.evaluated_params[m.getVariableName()] = evaluated_param_value.item()
            else:
                self.evaluated_params[m.getVariableName()] = evaluated_param_value


        loss = 0
        for i in range(len(self.dependencies[init_model])):
            m = self.dependencies[init_model][i]
            evaluated_optimisation_parameter_value = m.eval(self.evaluated_params, param_dependencies)
            self.evaluated_params[m.getVariableName()] = evaluated_optimisation_parameter_value
            
            #calculate loss for param
            loss += np.power(evaluated_optimisation_parameter_value-x0[i], 2)
            loss = np.sqrt(loss/len(self.dependencies[init_model]))
        
        print (loss, "\t->", self.evaluated_params)
        return loss



class ULTIMATE_Dependency:
    def __init__(self, dependent_model, source_model, property, variable, params=None, pmc=None, path=None):
        self.__dependent_model = dependent_model
        self.__source_model = source_model
        self.__property_str = property
        self.__variable = variable
        self.__pmc = pmc
        self.__path = path
        self.__params = params

        #read source model
        with open(source_model, "r") as model_file:
            self.source_model_content = model_file.read()


    def getVariableName(self):
        return self.__variable

    def getSourceModel(self):
        return self.__source_model

    def getDependentModel(self):
        return self.__dependent_model


    def eval(self, values, param_dependencies):
        if self.__pmc is PMC.Prism:
            #Solution 1: Appending to the template file (EvoChecker style)
            # result = self.invokePrism(values, param_dependencies)

            #Solution 2: use the constant flag
            result = self.invokePrism2(values, param_dependencies)
            return result
        else:     
            #Solution 3: use Storm
            result = self.invokeStorm(values, param_dependencies)
            return result
    

    def prepareTempFile(self, values):
        source_model_temp = self.source_model + "_temp"

        #prepare new content        
        new_content = "\n\n"
        for k in values.keys():
            new_content += "const double " + k + " = "
            if isinstance(values[k], np.float64):
                new_content += str(values[k].item()) +";\n"
            else:
                new_content += str(values[k]) +";\n"

        #save temp file
        with open(source_model_temp, "w") as model_file:
            model_file.write(self.source_model_content + new_content)

        return source_model_temp


    def invokePrism(self, values, param_dependencies):
        ''' Verify the provided model by invoking Prism
        '''
        #prepare temporary model file
        source_model_temp = self.prepareTempFile(values)

        # run PRISM
        # result = subprocess.run([self.pcm, model_file, property_file], capture_output=True, text=True)
        result = subprocess.run([self.__pcm, source_model_temp, "-pf", self.__property_str], capture_output=True, text=True)

        # parse output
        v = self.parsePMCResult(result, Prism=True)
        
        return v

    
    def invokePrism2(self, values, param_dependencies):
        '''
            Use the proposed values and verify the model using the const flag
        '''
        values_str = ""
        for k in values.keys():
            values_str += k + "="
            if isinstance(values[k], np.float64):
                values_str += str(values[k].item()) +","
            else:
                values_str += str(values[k]) +","
        values_str = values_str[:-1]

        # run PRISM
        result = subprocess.run([self.__path, self.__source_model, "-pf", self.__property_str, "-const", values_str ], capture_output=True, text=True)

        # parse output
        v = self.parsePMCResult(result)
        
        return v


    def parsePMCResult (self, result, Prism=True):
        # Print PRISM output
        # print("STDOUT:", result.stdout)
        if len(result.stderr)>0:
            print("ERROR:", result.stderr)

        # parse output
        output = result.stdout
        if Prism:
            match = re.search(r'Result: (\d+\.\d+)', output)  # Extract floating-point result
        else:
            match = re.search(r"Result \(for initial states\): ([+-]?\d*\.\d+)", output)
        if match:
            res = float(match.group(1))
        else:#if there is an issue with the verification - give back a very wrong/bad value
            res = 0
            #print(output)#sys.float_info.max
        
        # return
        return res


    def invokeStorm (self, values, param_dependencies):
        '''
            Use the proposed values and verify the model using the const flag
        '''
        params = []
        for p in param_dependencies.keys():
            if param_dependencies[p][0].getSourceModel() == self.getDependentModel():
                params.append(p)

        values_str = ""
        for k in values.keys():
            # if k not in self.__params:
            if k in params:
                values_str += k + "="
                if isinstance(values[k], np.float64):
                    values_str += str(values[k].item()) +","
                else:
                    values_str += str(values[k]) +","
        values_str = values_str[:-1]

        # run Storm
        result = subprocess.run([self.__path, "--prism", self.__source_model, "--prop", self.__property_str, "--constants", values_str ], capture_output=True, text=True)

        # parse output
        v = self.parsePMCResult(result, Prism=False)
        
        return v



class ULTIMATE_Dependency_Formula(ULTIMATE_Dependency):
    def __init__(self, dependent_model, source_model, equation, variable, params=None):
        super().__init__(dependent_model, source_model, equation, variable, params)
        #construct equation as a function 
        self.equation_func = compile(equation, "<string>", "eval")


    def eval(self, values):
        self.result = eval(self.equation_func, {}, values)
        return self.result




def parse_arguments():
    """
    Parse command line argument and initialise the ULTIMATE Solver 
    :return: a dictionary comprising the command-line arguments
    """
    text = 'ULTIMATE Numerical Solver'

    # initiate the parser
    parser = argparse.ArgumentParser(description=text)

    # new command-line arguments
    parser.add_argument("-I", "--input", help="input data", nargs='+', type=str, required=False)
    parser.add_argument("-P", "--path", help="model checker path", nargs=1, required=False, type=str)
    parser.add_argument("-C", "--mc", help="model checker",  nargs=1, required=False, type=str)
    parser.add_argument("-M", "--model", help="first model to check", nargs=1, required=False, type=str)
    args = parser.parse_args()

    return vars(args)



ULTIMATE_Dependency_MAP = {
    "PMC": ULTIMATE_Dependency,
    "Parametric": ULTIMATE_Dependency_Formula
}

class ULTIMATE_Solver_Enum(Enum):
    Parametric = 1
    Numerical  = 2


class PMC(Enum):
    Prism = 1
    Storm  = 2


if __name__ == "__main__":
    #parse command line arguments
    args = parse_arguments()
    if args['mc'][0].lower() ==  PMC.Prism.name.lower():
        pmc = PMC.Prism
    else:
        pmc = PMC.Storm
    path        = args['path'][0]
    input       = args['input']
    model_order = (args['model'])

    # init dependencies list
    dependencies_list = []

    # FOR TESTING
    # path = "/Users/simos/Documents/Software/prism-4.8-mac64-arm/bin/prism"
    # pmc = PMC.Prism
    # path = "storm"
    # pmc = PMC.Storm
    # 
    # model_order = ("select_perception_model.dtmc", "")
    # 
    # # Normal MC
    # input = [
        # "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect",
        # "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect", 
        # "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1",
        # "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2" 
    # ]
    # 
    #Parametric MC (note: set ULTIMATE_Solver_Enum.Parametric)
    # input = [
    #     "Models/RAD/select_perception_model.dtmc, Models/RAD/perceive-user2.dtmc, (4*pModel2+(-111)*pModel1+461)/(1000), pOkCorrect",
    #     "Models/RAD/select_perception_model.dtmc, Models/RAD/perceive-user2.dtmc, (-1 * (79*pModel2+2079*pModel1+(-9279)))/(20000), pNotOkCorrect",
    #     "Models/RAD/perceive-user2.dtmc, Models/RAD/select_perception_model.dtmc, (-953 * (pNotOkCorrect))/(1000 * (pOkCorrect+(-1)*pNotOkCorrect+(-1))), pModel1",
    #     "Models/RAD/perceive-user2.dtmc, Models/RAD/select_perception_model.dtmc, (2500*pOkCorrect+1669*pNotOkCorrect+(-2500))/(2500 * (pOkCorrect+(-1)*pNotOkCorrect+(-1))), pModel2"
    # ]
    #TESTING END

    for i in input:
        dep = []
        v = i.split(",")
        dep.append(v[0].strip()) #dependent model
        dep.append(v[1].strip()) #source model
        dep.append(v[2].strip()) #property/equation
        dep.append(v[3].strip()) #variable name in dependent
        
        params = None#[]
        # for param in v[3:]:
        #     param = param.strip()
        #     params.append(param)
        dep.append(params)
        #print(dep[0],"\t", dep[3], "\t", dep[4])
    
        dependencies_list.append(dep)    

    #create the ULTIMATE solver instance
    ulSolver = ULTIMATE_Solver(dependencies_list, pmc, path, ULTIMATE_Solver_Enum.Numerical)
    
    #given the SCC list, solve them based on the model order (starting from the model given)
    result, loss = ulSolver.optimise(model_order)

    # Print the optimal values for the parameters and the minimum value of the objective function
    print("Optimal parameters:", result)
    print("Minimum objective value:", loss)



## RUN COMMAND EXAMPLES##

#RAD - Storm invocation
#  python3 ULTIMATE_numerical_solver.py \
#  --path "storm" \
#  --mc "Storm" \
#  --model "select_perception_model.dtmc" \
#  --input "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect" "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2"

# RAD - Prism invocation
#  python3 ULTIMATE_numerical_solver.py \
#  --path "/Users/simos/Documents/Software/prism-4.8-mac64-arm/bin/prism" \
#  --mc "Prism" \
#  --model "select_perception_model.dtmc" \
#  --input "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (userOk & userPredictedOk))], pOkCorrect" "select_perception_model.dtmc, perceive-user2.dtmc, P=? [F (\"done\" & (!(userOk) & !(userPredictedOk)))], pNotOkCorrect" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=1], pModel1" "perceive-user2.dtmc, select_perception_model.dtmc, P=?[F s=2], pModel2"
 
