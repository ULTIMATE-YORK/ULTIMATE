package synthesis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import evochecker.lifecycle.IUltimate;
import ultimate.Ultimate;

public class EvoCheckerUltimateInstance implements IUltimate {

    private static final Logger logger = LoggerFactory.getLogger(EvoCheckerUltimateInstance.class);

    private Ultimate ultimate;
    private int executeCount = 0;

    // execsPerIndividual: execs per first param-group transition (works for
    // single-model synthesis where params only change between individuals).
    private HashMap<String, String> lastParams = null;
    private int execsForCurrentGroup = 0;
    private int execsPerIndividual = -1;

    // fullCycleExecs: execs per complete individual across all models (works for
    // multi-model synthesis where param keys differ between models within one
    // individual). Detected when the first model's param key set reappears after
    // a different key set was seen.
    private Set<String> firstParamKeys = null;
    private boolean sawDifferentParamKeys = false;
    private int execCountAtLastFirstKeyReturn = 0;
    private int fullCycleExecs = -1;

    // Logging: one entry per generation (every populationSize individuals).
    private int lastLoggedGeneration = 0;

    public EvoCheckerUltimateInstance(Ultimate ultimate) {
        this.ultimate = ultimate;
    }

    public void setTargetModelId(String id) {
        ultimate.setTargetModelById(id);
    }

    public void setInternalParameters(HashMap<String, String> internalParameterValuesHashMap) {
        ultimate.setInternalParameterValuesMap(internalParameterValuesHashMap);
        if (!internalParameterValuesHashMap.equals(lastParams)) {
            Set<String> keys = internalParameterValuesHashMap.keySet();

            // Full-cycle detection for multi-model synthesis.
            if (firstParamKeys == null) {
                firstParamKeys = new HashSet<>(keys);
                execCountAtLastFirstKeyReturn = executeCount;
            } else if (keys.equals(firstParamKeys)) {
                if (sawDifferentParamKeys && fullCycleExecs < 0) {
                    fullCycleExecs = executeCount - execCountAtLastFirstKeyReturn;
                    logger.info("Detected {} execute calls per individual (full cycle)", fullCycleExecs);
                }
                execCountAtLastFirstKeyReturn = executeCount;
                sawDifferentParamKeys = false;
            } else {
                sawDifferentParamKeys = true;
            }

            // Simple fallback: first param-group transition gives execs per group.
            if (execsPerIndividual < 0 && execsForCurrentGroup > 0) {
                execsPerIndividual = execsForCurrentGroup;
            }
            execsForCurrentGroup = 0;
            lastParams = new HashMap<>(internalParameterValuesHashMap);
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
        executeCount++;
        execsForCurrentGroup++;

        // Provide intra-batch progress so the counter advances during the parallel
        // evaluator's blocking batch call, not only after it completes.
        int effectiveExecs = fullCycleExecs > 0 ? fullCycleExecs
                : (execsPerIndividual > 0 ? execsPerIndividual : -1);
        if (effectiveExecs > 0) {
            int maxEval = ultimate.getMaxEvaluations();
            int populationSize = ultimate.getPopulationSize();
            int intraIndividuals = executeCount / effectiveExecs;
            int progress;
            if (populationSize <= maxEval) {
                // P <= maxEval: multiple batches expected; individual count maps
                // directly onto the progress scale.
                progress = Math.min(intraIndividuals, maxEval);
            } else {
                // P > maxEval: the initial batch alone exceeds maxEval, so scale
                // intra-batch individual count proportionally to maxEval so the
                // counter advances across the full batch rather than hitting max
                // after the first maxEval individuals.
                progress = Math.min(intraIndividuals * maxEval / populationSize, maxEval);
            }
            if (progress > ultimate.getSynthesisProgress()) {
                ultimate.setSynthesisProgress(progress);
                Runnable callback = ultimate.getUpdateProgressCallback();
                if (callback != null) {
                    callback.run();
                }
            }
        }
    }

    public void setVerificationProperty(String propertyFileOrString) {
        ultimate.setVerificationProperty(propertyFileOrString);
    }

    public HashMap<String, String> getResults() {
        return ultimate.getVerificationResultsMap();
    }

    public void updateSynthesisProgress(int evaluations) {
        // Called by pNSGAII after each parallel batch completes.
        // Cap to maxEval to handle the P > maxEval initial-batch over-count.
        int maxEval = ultimate.getMaxEvaluations();
        int cappedEval = Math.min(evaluations, maxEval);

        if (cappedEval > ultimate.getSynthesisProgress()) {
            ultimate.setSynthesisProgress(cappedEval);
            Runnable callback = ultimate.getUpdateProgressCallback();
            if (callback != null) {
                callback.run();
            }
        }

        // Log once per generation (every populationSize individual evaluations).
        int generation = evaluations / ultimate.getPopulationSize();
        if (generation > lastLoggedGeneration) {
            lastLoggedGeneration = generation;
            logger.info("Synthesis iteration {}/{} complete", cappedEval, maxEval);
        }
    }

}
