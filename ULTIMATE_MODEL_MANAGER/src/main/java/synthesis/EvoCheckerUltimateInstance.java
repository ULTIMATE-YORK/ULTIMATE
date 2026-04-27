package synthesis;

import java.util.HashMap;

import evochecker.lifecycle.IUltimate;
import ultimate.Ultimate;

public class EvoCheckerUltimateInstance implements IUltimate {

    private Ultimate ultimate;
    private HashMap<String, String> lastParams = null;

    public EvoCheckerUltimateInstance(Ultimate ultimate) {
        this.ultimate = ultimate;
    }

    public void setTargetModelId(String id) {
        ultimate.setTargetModelById(id);
    }

    public void setInternalParameters(HashMap<String, String> internalParameterValuesHashMap) {
        ultimate.setInternalParameterValuesMap(internalParameterValuesHashMap);
        // setInternalParameters is called once per objective per chromosome; detect a
        // new chromosome by checking whether the parameter values actually changed.
        if (!internalParameterValuesHashMap.equals(lastParams)) {
            lastParams = new HashMap<>(internalParameterValuesHashMap);
            ultimate.setSynthesisProgress(ultimate.getSynthesisProgress() + 1);
            Runnable callback = ultimate.getUpdateProgressCallback();
            if (callback != null) {
                callback.run();
            }
        }
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
        // Called by EvoChecker every ~POPULATION_SIZE evaluations; use only to
        // correct any drift from the parameter-change detection above.
        ultimate.setSynthesisProgress(evaluations);
        Runnable callback = ultimate.getUpdateProgressCallback();
        if (callback != null) {
            callback.run();
        }
    }

}
