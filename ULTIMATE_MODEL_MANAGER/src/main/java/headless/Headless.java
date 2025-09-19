package headless;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mariuszgromada.math.mxparser.License;

import data.SynthesisRun;
import data.VerificationResult;
import data.VerificationRun;
import evochecker.exception.EvoCheckerException;
import javafx.collections.ObservableList;
import parameters.InternalParameter;
import project.Project;
import sharedContext.SharedContext;
import synthesis.EvoCheckerUltimateInstance;
import ultimate.Ultimate;
import verification.VerificationException;

public class Headless {

	private static Options options = new Options();
	private static String projectFilePath = null;
	private static String outputDir = null;
	private static String modelId = null;
	private static String property = null;
	private static boolean help = false;

	private static void setUpCLI() {
		Option help = new Option("help", "prints usage help inforamtion");

		Option projectFile = Option.builder("pf").argName("file").hasArg()
				.desc("The file path to the ULTIAMTE project (world model) file").build();

		Option modelID = Option.builder("m").argName("modelID").hasArg()
				.desc("The ID of the model as described by the project file").build();

		Option property = Option.builder("p").argName("definition or file").hasArg()
				.desc("The definition of a property OR a path to a .pctl file").build();

		Option outputDir = Option.builder("o").argName("output directory").hasArg()
				.desc("The output directory for the results file. If left unspecified, no file will be created.")
				.build();

		options.addOption(projectFile);
		options.addOption(modelID);
		options.addOption(property);
		options.addOption(outputDir);
		options.addOption(help);
	}

	private static void getArgs(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);
			projectFilePath = line.getOptionValue("pf");
			modelId = line.getOptionValue("m");
			property = line.getOptionValue("p");
			outputDir = line.getOptionValue("o");

			if (line.hasOption("help")) {
				help = true;
			}
		} catch (ParseException e) {
			System.err.println("Parsing failed.  Reason: " + e.getMessage());
		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException {

		// create the parser
		System.setProperty("slf4j.internal.verbosity", "WARN");
		// License.iConfirmNonCommercialUse("ULTIMATE"); // Add this line to confirm
		// license for math lib

		setUpCLI();
		getArgs(args);

		if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("headless", options);
			return;
		}

		System.out.println("\n========  ULTIMATE --- Model Ensemble Verification Tool  ========\n\nProject file: "
				+ Paths.get(projectFilePath).getFileName().toString() + "\n");
		try {
			SharedContext.setProject(new Project((projectFilePath)));
		} catch (IOException e) {
			System.out.println("Could not find a valid project at " + projectFilePath);
		}
		Ultimate ultimate = SharedContext.getUltimateInstance();
		ultimate.loadModelsFromProject();

		if (SharedContext.getProject().containsRangedParameters()) {
			System.err.println("Ranged external parameters were found in the project."
					+ "\nHeadless mode does not yet support experiments (projects with ranged external parameters)."
					+ "\nPlease use the GUI version of ULTIMATE if you require this functionality.");
			System.exit(1);
		}

		ObservableList<InternalParameter> internalParameters = SharedContext.getProject().getAllInternalParameters();
		if (internalParameters.size() > 0) {
			handleSynthesis();
		} else {
			handleVerification();
		}

		// TODO: systemise the output by creating something like a OutputGenerator class
		// TODO: maybe also write some utility to stylise the output, e.g.
		// OutputUtility.printHeader()
	}

	public static void handleSynthesis() {
		System.out.println("Internal parameters found in the project file --- beginning a parameter synthesis problem."
				+ "\nULTIMATE uses EvoChecker for synthesis. If you would like to adjust the parameters of EvoChecker, please edit evochecker_config.properties.\n");
		try {
			SharedContext.getUltimateInstance().initialiseSynthesis();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"Could not initialise the synthesis problem. Do you have permission to write to the default temporary file directory?");
		}
		System.out.println("Running EvoChecker to synthesise parameters...");
		String runId = "Synth_" + UUID.randomUUID().toString() + "_" + SharedContext.getProject().getProjectName();
		try {
			SharedContext.getUltimateInstance().executeSynthesis(runId);
		} catch (IOException e) {
			System.err.println("An IO error occurred during synthesis: " + e.getMessage());
			e.printStackTrace();
			return;
		} catch (EvoCheckerException e) {
			System.err.println("An exception occurred whilst running EvoChecker: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		SynthesisRun run = SharedContext.getUltimateInstance().getSynthesisRun();

		if (outputDir == null || outputDir.equals("")) {
			System.out.println(
					"No output directory provided with -o, setting to current directory: " + System.getenv("PWD"));
			outputDir = System.getenv("PWD");
		}

		String exportPath = outputDir + "/" + runId + "_solutions";
		run.exportSolutions(outputDir + "/" + runId + "_solutions");
		System.out.println("Saved solutions to " + exportPath);

		String parameterNames = SharedContext.getProject().getAllInternalParameters().stream()
				.map((InternalParameter x) -> (x.getNameInModel())).collect(Collectors.joining("\n\t"));

		System.out.println(
				"\n========  Results  ========\n\nULTIMATE project:" + projectFilePath + "\nProblem type: Synthesis"
						+ "\nModel ID: " + modelId + "\nInternal Parameters:\n\t" + parameterNames);

	}

	public static void handleVerification() {
		System.out.println("Beginning a verification problem.");
		SharedContext.getUltimateInstance().setTargetModelById(modelId);
		SharedContext.getUltimateInstance().setVerificationProperty(property);
		String runId = "Ver_" + UUID.randomUUID().toString() + "_" + SharedContext.getProject().getProjectName();
		VerificationRun run = new VerificationRun(runId, modelId, property, false);
		try {
			VerificationResult result = SharedContext.getUltimateInstance().executeVerification();
			run.addResult(result);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(
					"IO exception occurred during verification. Either the temporary model files could not be created or, if you specified the properties and a file, it could not be read.");
		} catch (VerificationException e) {
			e.printStackTrace();
			throw new RuntimeException("An exception occurred in the verification process: " + e.getMessage());
		}

		if (outputDir == null || outputDir.equals("")) {
			System.out.println("No output directory provided, setting to current directory: " + System.getenv("PWD"));
			outputDir = System.getenv("PWD");
		}

		String exportPath = outputDir + "/" + runId + "_results";
		try {
			run.exportToFile(exportPath, false);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Could not write the verification results to " + exportPath);
		}

		String resultsInfo = SharedContext.getUltimateInstance().getVerificationResultsInfo();
		System.out.println("\n========  Results  ========\n\nULTIMATE project:" + projectFilePath
				+ "\nProblem type: Verification" + "\nModel ID: " + modelId + "\nProperties: "
				+ (property == null ? "(none specified - checked all)" : property) + "\n\n" + "Property Values:\n"
				+ resultsInfo);
	}
}
