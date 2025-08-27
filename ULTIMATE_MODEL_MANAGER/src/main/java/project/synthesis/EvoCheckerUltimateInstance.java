package project.synthesis;

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

    //TODO: remove this in IUltimate
    public void generateModelInstances() {
        // ultimate.writeParametersToModelFiles();
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

    // TODO: In EvoChecker: conform to the new data type HashMap<String, String>.
    public HashMap<String, String> getResults() {
        return ultimate.getVerificationResults();
    }

    public void updateSynthesisProgress(int evaluations) {
        ultimate.setSynthesisProgress(evaluations);
    }

}
