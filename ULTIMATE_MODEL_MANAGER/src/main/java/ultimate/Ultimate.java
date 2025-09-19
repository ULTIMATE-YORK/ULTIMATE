package ultimate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import data.SynthesisRun;
import data.SynthesisSolution;
import data.VerificationResult;
import evochecker.EvoChecker;
import evochecker.exception.EvoCheckerException;
import evochecker.lifecycle.IUltimate;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import model.Model;
import parameters.IStaticParameter;
import project.Project;
import project.ProjectExporter;
import property.Property;
import sharedContext.SharedContext;
import synthesis.EvoCheckerUltimateInstance;
import utils.FileUtils;
import verification.NPMCVerification;
import verification.VerificationException;

public class Ultimate {

    // private Project project;
    // private String projectFilePath;

    private Model targetModel;

    private NPMCVerification verifier;
    private ArrayList<Model> models = new ArrayList<>();

    private String property;
    private String objectivesConstraints;

    private HashMap<String, String> internalParameterValuesHashMap = new HashMap<>();

    private EvoChecker evoChecker;
    private Path evolvableProjectFilePath;

    private String mode;
    private int synthesisProgress;

    private boolean verbose;
    private boolean modelParametersWritten;
    private Runnable updateCallback;

    private SynthesisRun synthesisRun;

    java.util.HashMap<String, String> verificationResultsMap = new java.util.HashMap<>();

    public Ultimate() {
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void loadModelsFromProject() {
        models = new ArrayList<>();
        Set<Model> projectModels = SharedContext.getProject().getModels();
        if (projectModels == null || projectModels.size() == 0) {
            System.err
                    .println("Warning: ULTIMATE tried to initialise models, but no models were found in the project.");
        }
        models.addAll(projectModels);
        verifier = new NPMCVerification(models);
    }

    private void writeParametersToModelFiles() throws IOException {
        for (Model m : models) {
            m.setInternalParameterValuesFromMap(internalParameterValuesHashMap);
            // m.setExternalParameterValuesFromMap(externalParameterValuesHashMap);
            FileUtils.writeParametersToFile(m.getVerificationFilePath(), m.getExternalParameters(),
                    m.getInternalParameters());
        }
        modelParametersWritten = true;

    }

    public void setInternalParameterValuesMap(HashMap<String, String> internalParameterValuesHashMap) {
        modelParametersWritten = false;
        this.internalParameterValuesHashMap = internalParameterValuesHashMap;
    }

    // public void setExternalParameterValuesMap(HashMap<String, ?> params) {
    // modelParametersWritten = false;
    // HashMap<String, String> hm = new HashMap<>();
    // for (Map.Entry<String, ?> e : params.entrySet()) {
    // hm.put(e.getKey(), e.getValue().toString());
    // }
    // this.externalParameterValuesHashMap = hm;
    // }

    public void setTargetModelById(String modelID) {

        if (models == null) {
            throw new RuntimeException("'Models' was null. Did you call .loadModelFromProject?");
        }

        Model matchingModel = models.stream().filter(model -> model.getModelId().equals(modelID)).findFirst()
                .orElse(null);
        this.targetModel = matchingModel;

        if (this.targetModel == null) {
            throw new IllegalArgumentException("Model ID not found: " + modelID); // Fail if not found
        }
    }

    public void generateEvolvableModelFiles() throws IOException {

        Project project = SharedContext.getProject();

        if (project == null) {
            throw new RuntimeException("Could not find the project in the SharedContext. Have both been initialised?");
        }

        if (models == null) {
            throw new RuntimeException("'Models' was null. Did you call .loadModelFromProject?");
        }

        Path tempDir = Files.createTempDirectory("ultimate_evoproject_");
        Path sourceProjectPath = Paths.get(project.getProjectFilePath());
        Path targetProjectPath = tempDir.resolve(sourceProjectPath.getFileName());
        evolvableProjectFilePath = targetProjectPath;
        Files.copy(sourceProjectPath, targetProjectPath, StandardCopyOption.REPLACE_EXISTING);

        // write the synthesis objectives into the temporary file
        ProjectExporter exporter = new ProjectExporter(SharedContext.getProject());
        exporter.saveExport(targetProjectPath.toString());

        for (Model m : models) {

            Path sourceModelPath = sourceProjectPath.resolve(m.getFilePath());
            Path targetModelPath = tempDir.resolve(sourceModelPath.getFileName());
            Files.copy(sourceModelPath, targetModelPath, StandardCopyOption.REPLACE_EXISTING);

            FileUtils.writeEvolvablesToFile(targetModelPath.toString(), m.getInternalParameters());
            FileUtils.writeParametersToFile(targetModelPath.toString(), m.getExternalParameters(),
                    null);
            FileUtils.writeDummyDependenciesToFile(targetModelPath.toString());
        }

    }

    public VerificationResult executeVerification() throws IOException, VerificationException {
        if (verbose)
            System.out.print("Executing ULTIMATE for property " + property);
        resetResults();
        if (!modelParametersWritten) {
            writeParametersToModelFiles();
        }
        verificationResultsMap = new HashMap<>();
        if (property == null) {
            for (Property p : targetModel.getProperties()) {
                if (verbose)
                    System.out.println("Verifying property: " + p.getDefinition());
                String result_value = verifier.verify(targetModel.getModelId(), p.getDefinition());
                verificationResultsMap.put(p.toString(), result_value);
            }
        } else {
            // this checks if the property provided is a file or a property entered as a
            // string (e.g. from the CLI)
            if (new java.io.File(property).isFile()) { // file -> read all lines and verify
                for (String line : java.nio.file.Files.readAllLines(java.nio.file.Paths.get(property))) {
                    if (verbose)
                        System.out.println("Verifying property: " + line.trim());
                    if (!line.trim().isEmpty()) {
                        String result_value = verifier.verify(targetModel.getModelId(), line.trim());
                        verificationResultsMap.put(line.trim(), result_value);
                    }
                }
            } else { // string -> verify single property
                String result_value = verifier.verify(targetModel.getModelId(), property);
                if (verbose)
                    System.out.println(property + ": " + result_value);
                verificationResultsMap.put(property, result_value);
            }
        }

        VerificationResult vr = new VerificationResult(targetModel.getModelId(), property, verificationResultsMap);
        return vr;

    }

    // public void synthesiseInternalParameters() {
    // /*
    // * - Instantiate EC
    // * - Initialise EC
    // * - Write evolvable world model (EWM) files
    // * - Give EC the OCs, the EWMs, and the ULTIMATE instance itself
    // * - Set results
    // */

    // }

    public EvoChecker getEvoCheckerInstance() {
        return evoChecker;
    }

    public void createEvoCheckerInstance(IUltimate ultimateInstance) {
        evoChecker = new EvoChecker();
        evoChecker.setUltimateInstance(ultimateInstance);
    }

    public void initialiseEvoCheckerInstance(String projectFile) {
        if (evoChecker == null) {
            throw new RuntimeException(
                    "Attempted to initialise EvoChecker, but it has not yet been instantiated. Call 'createEvoCheckerInstance' first");
        }
        evoChecker.setConfigurationFile(System.getenv("ULTIMATE_DIR") + "/evochecker_config.properties", projectFile,
                null);
    }

    public SynthesisRun executeSynthesis(String runId) throws EvoCheckerException, IOException {
        if (evoChecker == null) {
            throw new RuntimeException(
                    "Attempted to run synthesis, but EvoChecker has not yet been instantiated. Call 'createEvoCheckerInstance' first");
        }
        try {
        	evoChecker.start();
	        evoChecker.printStatistics();
	        System.out.println("Finished EvoChecker synthesis");
	        writeSynthesisResultsToFile("/tmp", true);
	        synthesisRun = createSynthesisRun(runId);
        } catch (EvoCheckerException e) {
        	terminateEvoChecker();
        	throw new EvoCheckerException(e.getMessage());
        }
        terminateEvoChecker();
        
        System.out.println("\nSynthesis complete.");
        
        return synthesisRun;        
    }

    public void terminateEvoChecker() {
    	evoChecker=null;
        System.out.println("\n========  Terminating EvoChecker...  ========\n");
    }
    
    public SynthesisRun getSynthesisRun() {
        return synthesisRun;
    }

    public void setObjectivesConstraints(String propertyFileOrString) {
        this.objectivesConstraints = propertyFileOrString;
    }

    public void setVerificationProperty(String propertyFileOrString) {
        this.property = propertyFileOrString;
    }

    public void setVerificationProperty(Property property) {
        this.property = property.toString();
    }

    public java.util.HashMap<String, String> getVerificationResultsMap() {
        return verificationResultsMap;
    }

    //TODO: 'directory' is not used if makeTemporary is true. Find a better solution  
    public void writeSynthesisResultsToFile(String directory, boolean makeTemporary) {

        try {
            evoChecker.ExportToFile(directory, makeTemporary);
        } catch (Exception e) {
            System.err.println("Couldn't write the synthesis results to file.");
            e.printStackTrace();
        }

    }

    public void resetResults() {
        verificationResultsMap = new java.util.HashMap<>();
    }

    public String generateModelConfigurationIdentifier() throws IOException {

        StringBuilder configIdBuilder = new StringBuilder();

        for (Model m : this.models) {
            for (IStaticParameter p : m.getStaticParameters()) {
                configIdBuilder.append(p.getNameInModel() + " : " + p.getValue() + "\n");
            }
        }

        return configIdBuilder.toString();
    }

    public HashMap<String, String> generateModelConfigurationHashMap() throws IOException {

        HashMap<String, String> modelConfigurationHashMap = new HashMap<>();

        // save all eps, ips, to string
        for (Model m : this.models) {
            for (IStaticParameter p : m.getStaticParameters()) {
                modelConfigurationHashMap.put(p.getNameInModel(), p.getValue());
            }
        }

        return modelConfigurationHashMap;

    }

    public String getVerificationResultsInfo() {

        StringBuilder resultsInfo = new StringBuilder();
        for (String key : verificationResultsMap.keySet()) {
            resultsInfo.append("Property: " + key + "\nResult: " + verificationResultsMap.get(key) + "\n\n");
        }
        return resultsInfo.toString();

    }

    public String getTargetModelId() {
        return targetModel.getModelId();
    }

    public String getProperty() {
        return property;
    }

    public String getObjectivesConstraints() {
        return objectivesConstraints;
    }

    public ArrayList<HashMap<String, String>> getSynthesisParetoFront() {


        ArrayList<HashMap<String, String>> results = new ArrayList<>();
        SolutionSet evoSolutions = evoChecker.getSolutions();
        List<evochecker.properties.Property> evoObjectives = evoChecker.getObjectives();
        for (int i = 0; i < evoSolutions.getCapacity(); i++) {
            Solution s = evoSolutions.get(i);
            HashMap<String, String> r = new HashMap<>();
            for (int j = 0; j < evoObjectives.size(); j++) {
                r.put("Objective " + evoObjectives.get(j).getExpression(), String.valueOf(s.getObjective(j)));
            }
            results.add(r);
        }

        return results;
    }

    public ArrayList<HashMap<String, String>> getSynthesisParetoSet() {

        String fileName = evoChecker.getParetoSetFileName();
        ArrayList<HashMap<String, String>> results = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));

            // Read header
            String headerLine = br.readLine();
            String[] columnNames = headerLine.trim().split("\\s+");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty())
                    continue; // skip empty lines
                String[] tokens = line.trim().split("\\s+");
                HashMap<String, String> rowMap = new HashMap<>();
                for (int i = 0; i < tokens.length; i++) {
                    rowMap.put(columnNames[i], tokens[i]);
                }
                results.add(rowMap);
            }
            br.close();

        } catch (IOException e) {
            System.err.println("The pareto set file was not found or could not be opened.");
        }
        return results;

    }

    public Model getTargetModel() {
        return targetModel;
    }

    public Path getEvolvableProjectFilePath() {
        return evolvableProjectFilePath;
    }

    public void setSynthesisProgress(int progress) {
        synthesisProgress = progress;
    }

    public int getSynthesisProgress() {
        return synthesisProgress;
    }

    public void setSynthesisUpdateCallback(Runnable runnable) {
        this.updateCallback = runnable;
    }

    public Runnable getUpdateProgressCallback() {
        return updateCallback;
    }

    public void plotParetoFront() {
        evoChecker.plotParetoFront();

    }

    public void initialiseSynthesis() throws IOException {
        generateEvolvableModelFiles();
        String evolvableProjectFileDir = getEvolvableProjectFilePath().toString();
        EvoCheckerUltimateInstance ultimateInstance = new EvoCheckerUltimateInstance(this);
        createEvoCheckerInstance(ultimateInstance);
        initialiseEvoCheckerInstance(evolvableProjectFileDir);
    }

    private SynthesisRun createSynthesisRun(String runId) {
        SynthesisRun run = new SynthesisRun(runId);

        ArrayList<HashMap<String, String>> synthesisFront = getSynthesisParetoFront();
        ArrayList<HashMap<String, String>> synthesisSet = getSynthesisParetoSet();

        List<SynthesisSolution> runSolutions = new ArrayList<>();
        for (int i = 0; i < synthesisFront.size(); i++) {

            HashMap<String, String> internalParameterValues = synthesisSet.get(i);
            HashMap<String, String> objectiveValues = synthesisFront.get(i);

            SynthesisSolution solution = new SynthesisSolution(
                    Integer.toString(i),
                    internalParameterValues,
                    objectiveValues);

            runSolutions.add(solution);
        }

        run.addSolutions(runSolutions);
        run.setParetoFrontFilePath(getEvoCheckerInstance().getParetoFrontFileName());
        run.setParetoSetFilePath(getEvoCheckerInstance().getParetoSetFileName());

        return run;
    }

}