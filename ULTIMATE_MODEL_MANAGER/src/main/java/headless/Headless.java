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

import ultimate.Ultimate;

public class Headless {

	private static Options options = new Options();
	private static String projectFile = null;
	private static String modelID = null;
	private static String property = null;
	private static boolean help = false;

	private static void setUpCLI() {
		Option help = new Option("help", "prints usage help inforamtion");

		Option projectFile = Option.builder("pf")
				.argName("file")
				.hasArg()
				.desc("The file path to the world model ultimate file")
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

		options.addOption(projectFile);
		options.addOption(modelID);
		options.addOption(property);
		options.addOption(help);
	}

	private static void getArgs(String[] args) {
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(options, args);
			projectFile = line.getOptionValue("pf");
			modelID = line.getOptionValue("m");
			property = line.getOptionValue("p");

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

		Ultimate ultimate = new Ultimate();

		ultimate.loadProjectFromFile(projectFile);
		ultimate.setModelID(modelID);
		ultimate.setProperty(property);
		ultimate.execute();
		java.util.HashMap<String, Double> results = ultimate.getResults();
		String resultsInfo = ultimate.getResultsInfo();

		System.out.println("\n========  Results  ========\n\nULTIMATE project:" + projectFile + "\nModel ID: " + modelID
				+ "\nProperties: " + (property == null ? "(none specified - checked all)" : property) + "\n\n" + "Property Values:\n"+ resultsInfo);

		// Write the results HashMap to a file
		String fileName = "ultimate_results_" +
				(projectFile != null ? new java.io.File(projectFile).getName().replaceAll("\\W+", "_") : "unknown") +
				"_" +
				(modelID != null ? modelID.replaceAll("\\W+", "_") : "unknown") +
				"_" +
				java.time.LocalDateTime.now().toString().replaceAll("[:.]", "-") +
				".txt";

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
