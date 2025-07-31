package verification;

import java.util.HashMap;

import evochecker.lifecycle.IUltimate;
import ultimate.Ultimate;

public class EvoCheckerUltimateInstance implements IUltimate {

    private Ultimate ultimate;

    public EvoCheckerUltimateInstance(Ultimate ultimate){
        this.ultimate = ultimate;
    }

    public void setTargetModelId(String id) {
        ultimate.setTargetModelID(id);
    }

    public void setInternalParameters(HashMap<String, String> internalParameterValuesHashMap) {
        ultimate.setInternalParameters(internalParameterValuesHashMap);
    }

    public void generateModelInstances() {
        ultimate.generateModelInstances();
    }

    public void resetResults() {
        ultimate.resetResults();
    }

    public void execute() {
        try {
            ultimate.execute();
        } catch (Exception e) {
            System.err.println("Error executing ULTIMATE.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setVerificationProperty(String propertyFileOrString){
        ultimate.setVerificationProperty(propertyFileOrString);
    }

    public HashMap<String, Double> getResults(){
        return ultimate.getResults();
    }

    
}
