package headless;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import evochecker.EvoChecker;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import parameters.InternalParameter;
import org.mariuszgromada.math.mxparser.mXparser;

import ultimate.Ultimate;
import verification.EvoCheckerUltimateInstance;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;

public class Headless {

	private static Options options = new Options();
	private static String projectFile = null;
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
				.argName("directory")
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
			projectFile = line.getOptionValue("pf");
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
		setUpCLI();
		getArgs(args);

		if (help) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("headless", options);
			return;
		}

		System.out.println("\n========  ULTIMATE --- Model Ensemble Verification Tool  ========\nProject file: " + Paths.get(projectFile).getFileName().toString()+"\n");
		Ultimate ultimate = new Ultimate();
		ultimate.loadProject(projectFile);
		ultimate.setTargetModelID(modelID);


		if (ultimate.getProject().containsRangedParameters()) {
			System.err.println(
					"Ranged external parameters were found in the project."
							+ "\nHeadless mode does not yet support experiments (projects with ranged external parameters)."
							+ "\nPlease use the GUI version of ULTIMATE if you require this functionality.");
			System.exit(1);
		}

		ObservableList<InternalParameter> internalParameters = ultimate.getInternalParameters();
		if (internalParameters.size() > 0) {
			System.out.println(
					"Internal parameters found in the project file --- beginning a parameter synthesis problem."
							+ "\nULTIMATE uses EvoChecker for synthesis. If you would like to adjust the parameters of EvoChecker, please edit evochecker_config.properties.\n");
			ultimate.generateEvolvableModelFiles();
			String evolvableProjectFileDir = ultimate.getEvolvableProjectFilePath().toString();
			EvoCheckerUltimateInstance ultimateInstance = new EvoCheckerUltimateInstance(ultimate);
			ultimate.instantiateEvoCheckerInstance(ultimateInstance);
			ultimate.initialiseEvoCheckerInstance(evolvableProjectFileDir);
			System.out.println("Running EvoChecker to synthesise parameters...\n");
			ultimate.executeEvoChecker();
			ultimate.writeSynthesisResultsToFile();

		} else {
			System.out.println("Beginning a verification problem.\n");
			ultimate.setVerificationProperty(property);
			ultimate.generateModelInstances();
			ultimate.execute();
		}

		java.util.HashMap<String, Double> results = ultimate.getResults();
		String resultsInfo = ultimate.getVerificationResultsInfo();
		// TODO: systemise the output by creating something like a OutputGenerator class
		// TODO: maybe also write some utility to stylise the output, e.g. OutputUtility.printHeader()
		if (internalParameters.size() > 0) {
			String parameterNames = internalParameters.stream()
					.map((InternalParameter x) -> (x.getName() + " - " + x.getType()))
					.collect(Collectors.joining("\n\t"));

			System.out.println("\n========  Results  ========\n\nULTIMATE project:" + projectFile
					+ "\nProblem type: Synthesis"
					+ "\nModel ID: " + modelID
					+ "\nInternal Parameters:\n\t" + parameterNames + "\n"
					+ "Results were saved to /data/ULTIMATE");
		} else {
			System.out.println("\n========  Results  ========\n\nULTIMATE project:" + projectFile
					+ "\nProblem type: Verification"
					+ "\nModel ID: " + modelID
					+ "\nProperties: " + (property == null ? "(none specified - checked all)" : property) + "\n\n"
					+ "Property Values:\n" + resultsInfo);
		}
		// Write the results HashMap to a file
		if (outputDir != null) {
			String fileName = outputDir + "/ultimate_results_" +
					(projectFile != null ? new java.io.File(projectFile).getName().replaceAll("\\W+", "_") : "unknown")
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
				for (java.util.Map.Entry<String, Double> entry : results.entrySet()) {
					writer.write(entry.getKey() + ": " + entry.getValue());
					writer.newLine();
				}
			}
		}

	}
}
