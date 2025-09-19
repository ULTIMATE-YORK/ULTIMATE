package synthesis;

import java.util.HashMap;

import evochecker.lifecycle.IUltimate;
import ultimate.Ultimate;

public class EvoCheckerUltimateInstance implements IUltimate {

    private Ultimate ultimate;

    public EvoCheckerUltimateInstance(Ultimate ultimate) {
        this.ultimate = ultimate;
    }

    public void setTargetModelId(String id) {
        ultimate.setTargetModelById(id);
    }

    public void setInternalParameters(HashMap<String, String> internalParameterValuesHashMap) {
        ultimate.setInternalParameterValuesMap(internalParameterValuesHashMap);
    }

    public void resetResults() {
        ultimate.resetResults();
    }

    public void execute() {
        try {
            ultimate.executeVerification();
        } catch (Exception e) {
            System.err.println("Error executing ULTIMATE.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void setVerificationProperty(String propertyFileOrString) {
        ultimate.setVerificationProperty(propertyFileOrString);
    }

    public HashMap<String, String> getResults() {
        return ultimate.getVerificationResultsMap();
    }

    public void updateSynthesisProgress(int evaluations) {
        
    }

}
