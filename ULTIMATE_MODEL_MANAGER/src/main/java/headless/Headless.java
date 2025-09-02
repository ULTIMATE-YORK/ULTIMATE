package headless;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.mariuszgromada.math.mxparser.License;

import javafx.collections.ObservableList;
import parameters.InternalParameter;
import project.synthesis.EvoCheckerUltimateInstance;
import sharedContext.SharedContext;
import ultimate.Ultimate;

public class Headless {

	private static Options options = new Options();
	private static String projectFilePath = null;
	private static String outputDir = null;
	private static String modelID = null;
	private static String property = null;
	private static boolean help = false;

	private static void setUpCLI() {
		Option help = new Option("help", "prints usage help inforamtion");

		Option projectFile = Option.builder("pf")
				.argName("file")
				.hasArg()
				.desc("The file path to the ULTIAMTE project (world model) file")
				.build();

		Option modelID = Option.builder("m")
				.argName("modelID")
				.hasArg()
				.desc("The ID of the model as described by the project file")
				.build();

		Option property = Option.builder("p")
				.argName("definition or file")
				.hasArg()
				.desc("The definition of a property OR a path to a .pctl file")
				.build();

		Option outputDir = Option.builder("o")
				.argName("output directory")
				.hasArg()
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
			modelID = line.getOptionValue("m");
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
		SharedContext.loadProjectFromPath(projectFilePath);
		Ultimate ultimate = SharedContext.getUltimateInstance();
		ultimate.loadModelsFromProject();

		if (SharedContext.getProject().containsRangedParameters()) {
			System.err.println(
					"Ranged external parameters were found in the project."
							+ "\nHeadless mode does not yet support experiments (projects with ranged external parameters)."
							+ "\nPlease use the GUI version of ULTIMATE if you require this functionality.");
			System.exit(1);
		}

		ObservableList<InternalParameter> internalParameters = SharedContext.getProject().getAllInternalParameters();
		if (internalParameters.size() > 0) {
			System.out.println(
					"Internal parameters found in the project file --- beginning a parameter synthesis problem."
							+ "\nULTIMATE uses EvoChecker for synthesis. If you would like to adjust the parameters of EvoChecker, please edit evochecker_config.properties.\n");
			ultimate.initialiseSynthesis();
			System.out.println("Running EvoChecker to synthesise parameters...");
			ultimate.executeSynthesis();
			ultimate.writeSynthesisResultsToFile();
		} else {
			System.out.println("Beginning a verification problem.");
			ultimate.setTargetModelById(modelID);
			ultimate.setVerificationProperty(property);
			ultimate.executeVerification();
		}

		String resultsInfo = ultimate.getVerificationResultsInfo();
		// TODO: systemise the output by creating something like a OutputGenerator class
		// TODO: maybe also write some utility to stylise the output, e.g.
		// OutputUtility.printHeader()
		if (internalParameters.size() > 0) {
			String parameterNames = internalParameters.stream()
					.map((InternalParameter x) -> (x.getNameInModel()))
					.collect(Collectors.joining("\n\t"));

			System.out.println("\n========  Results  ========\n\nULTIMATE project:" + projectFilePath
					+ "\nProblem type: Synthesis"
					+ "\nModel ID: " + modelID
					+ "\nInternal Parameters:\n\t" + parameterNames + "\n"
					+ "Results were saved to /data/ULTIMATE");
		} else {
			System.out.println("\n========  Results  ========\n\nULTIMATE project:" + projectFilePath
					+ "\nProblem type: Verification"
					+ "\nModel ID: " + modelID
					+ "\nProperties: " + (property == null ? "(none specified - checked all)" : property) + "\n\n"
					+ "Property Values:\n" + resultsInfo);
		}
		// Write the results HashMap to a file
		if (outputDir != null) {
			java.util.HashMap<String, String> results = ultimate.getVerificationResults();
			String fileName = outputDir + "/ultimate_results_" +
					(projectFilePath != null ? new java.io.File(projectFilePath).getName().replaceAll("\\W+", "_")
							: "unknown")
					+
					"_" +
					(modelID != null ? modelID.replaceAll("\\W+", "_") : "unknown") +
					"_" +
					java.time.LocalDateTime.now().toString().replaceAll("[:.]", "-") +
					".ultimate_output";

			System.out.println("Writing results to " + fileName);

			try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(
					java.nio.file.Paths.get(fileName),
					java.nio.charset.StandardCharsets.UTF_8)) {
				for (java.util.Map.Entry<String, String> entry : results.entrySet()) {
					writer.write(entry.getKey() + ": " + entry.getValue());
					writer.newLine();
				}
			}
		}

	}
}
