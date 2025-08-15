package utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import model.Model;
import parameters.InternalParameter;
import parameters.ExternalParameter;
import parameters.IParameter;
import parameters.IStaticParameter;
import parameters.ExternalParameter;
import java.util.List;
import java.lang.NullPointerException;

public class FileUtils {

	public static final String[] VALID_PRISM_FILE_EXTENSIONS = { "*.ctmc", "*.dtmc", "*.pomdp", "*.prism", "*.mdp" };
	public static final String VALID_ULT_FILE_EXTENSIONS = "*.ultimate";

	/**
	 * Check if a file is an existing prism file by checking the file exists and
	 * ends with a prism model file extension
	 * 
	 * @param filePath
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean isPrismFile(String filePath) throws IOException {
		if (isFile(filePath) && isPrismModelFile(filePath)) {
			return true;
		} else {
			throw new IOException("File at " + filePath + " does not exist or is not a prism model file");
		}
	}

	/*
	 * Check if a file is an existing ultimate file by checking the file extension
	 * 
	 * @param projectPath
	 * 
	 * @return boolean
	 */
	public static boolean isUltimateFile(String projectPath) throws IOException {
		if (isFile(projectPath) && projectPath.toLowerCase().endsWith(".ultimate")) {
			return true;
		} else {
			throw new IOException("File does not exist or is not an ultimate file");
		}
	}

	/**
	 * Check if a file is an existing file
	 * 
	 * @param filePath
	 * @return boolean
	 */
	public static boolean isFile(String filePath) {
		// check file exists
		return Files.exists(Paths.get(filePath));
	}

	/*
	 * Returns the name of a file without the file extension
	 * 
	 * @param filePath
	 * 
	 * @return String the file name without the file extension
	 */
	public static String removePrismFileExtension(String filePath) throws IOException {
		if (isPrismFile(filePath)) {
			Path path = Paths.get(filePath);
			String fileName = path.getFileName().toString(); // Get "c.prism"

			int lastDotIndex = fileName.lastIndexOf('.');
			return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
		}
		return null;
	}

	public static String removeFullPathPrism(String filePath) throws IOException {
		if (isPrismFile(filePath)) {
			Path path = Paths.get(filePath);
			return path.getFileName().toString(); // Get "c.prism"
		}
		return null;
	}

	/*
	 * Returns the name of a file without the file extension
	 * 
	 * @param filePath
	 * 
	 * @return String the file name without the file extension
	 */
	public static String removeUltimateFileExtension(String filePath) throws IOException {
		if (isUltimateFile(filePath)) {
			Path path = Paths.get(filePath);
			String fileName = path.getFileName().toString(); // Get "c.prism"

			int lastDotIndex = fileName.lastIndexOf('.');
			return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
		}
		return null;
	}

	public static String getFileContent(String filePath) throws IOException {
		if (isFile(filePath)) {
			return Files.readString(Paths.get(filePath));
		}
		return null;
	}

	// PRIVATE METHODS

	/*
	 * Check if a file is an existing prism model file by checking the file
	 * extension
	 * 
	 * @param filePath
	 * 
	 * @return boolean
	 */
	private static boolean isPrismModelFile(String filePath) {
		String[] extensions = new String[] { ".prism", ".ctmc", ".dtmc", ".mdp", ".pomdp" };
		for (String ext : extensions) {
			if (filePath.toLowerCase().endsWith(ext.toLowerCase())) {
				return true;
			}
		}
		return false;
	}

	public static void updateModelFileResults(Model model, HashMap<String, Double> constants) {
		String filePath = model.getFilePath();

		try {
			// Read all lines from the file
			Path path = Paths.get(filePath);
			StringBuilder updatedContent = new StringBuilder();

			for (String line : Files.readAllLines(path)) {
				String updatedLine = line;

				// Check if the line contains the pattern "const double NAME;"
				for (String key : constants.keySet()) {
					String pattern = "const double " + key + ";";
					if (line.contains(pattern)) {
						double value = constants.get(key);
						updatedLine = "const double " + key + " = " + value + ";";
						break; // Stop checking once a match is found for this line
					}
				}

				updatedContent.append(updatedLine).append(System.lineSeparator());
			}

			// Write the updated content back to the file
			Files.write(path, updatedContent.toString().getBytes());

		} catch (IOException e) {
			System.err.println("Error updating model file: " + e.getMessage());
		}
	}

	private static String findAndFillExternalParameter(String line, List<ExternalParameter> externalParameters)
			throws IOException {
		String updatedLine = null;
		// System.out.println(externalParameters.stream().map(ExternalParameter::getName).collect(Collectors.toList()));
		for (ExternalParameter ep : externalParameters) {
			// Updated regex to match "const <type> key = <value>;" or "const <type> key;"
			try {
				String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(ep.getName()) + "\\s*(=\\s*[^;]+)?;";
				Pattern pattern = Pattern.compile(regex);
				Matcher matcher = pattern.matcher(line);
				if (matcher.find()) {
					String valueType = matcher.group(1);
					updatedLine = "const " + valueType + " " + ep.getName() + " = " + ep.getValue() + ";";
					break;
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
				throw new RuntimeException(
						"Could not construct regex for " + ep.getClass().getName() + "with name " + ep.getName());
			}
		}
		return updatedLine;
	}

	// should make these the one function
	private static String findAndFillInternalParameter(String line, List<InternalParameter> internalParameters) {
		String updatedLine = null;
		for (InternalParameter ip : internalParameters) {
			// Updated regex to match "const <type> key = <value>;" or "const <type> key;"
			String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(ip.getName()) + "\\s*(=\\s*[^;]+)?;";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String valueType = matcher.group(1);
				updatedLine = "const " + valueType + " " + ip.getName() + " = " + ip.getValue() + ";";
				break;
			}
		}
		return updatedLine;
	}

	public static String writeParametersToModelString(String filePath, List<ExternalParameter> externalParameters,
			List<InternalParameter> internalParameters) {

		Path path = Paths.get(filePath);
		StringBuilder updatedContent = new StringBuilder();

		try {
			for (String line : Files.readAllLines(path)) {

				if (externalParameters != null) {
					String epUpdate = findAndFillExternalParameter(line, externalParameters);
					if (epUpdate != null) {
						updatedContent.append(epUpdate).append(System.lineSeparator());
						continue;
					}
				}

				if (internalParameters != null) {
					String ipUpdate = findAndFillInternalParameter(line, internalParameters);
					if (ipUpdate != null) {
						updatedContent.append(ipUpdate).append(System.lineSeparator());
						continue;
					}
				}
				updatedContent.append(line).append(System.lineSeparator());
			}
		} catch (Exception e) {
			System.err.println("Error reading file: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}

		return updatedContent.toString();
	}

	public static void writeParametersToFile(String filePath, List<ExternalParameter> externalParameters,
			List<InternalParameter> internalParameters) {

		Path path = Paths.get(filePath);
		String fileString = writeParametersToModelString(filePath, externalParameters, internalParameters);

		try {
			Files.write(path, fileString.getBytes());
		} catch (Exception e) {
			System.err.println("Error writing parameters to model file: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	// overflow for hashmaps
	public static void writeParametersToFile(String filePath, HashMap<String, ExternalParameter> hashExternalParameters,
			HashMap<String, InternalParameter> hashInternalParameters) {

		try {
			// Read all lines from the file
			Path path = Paths.get(filePath);
			StringBuilder updatedContent = new StringBuilder();

			// Process each line from the file
			for (String line : Files.readAllLines(path)) {
				String updatedLine = line;

				// For each key in the constants map, use regex to find a match for "const
				// <type> key;"

				if (hashExternalParameters.keySet().size() > 0) {
					for (String key : hashExternalParameters.keySet()) {
						// Updated regex to match "const <type> key = <value>;" or "const <type> key;"
						String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(key) + "\\s*(=\\s*[^;]+)?;";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(line);
						if (matcher.find()) {
							String type = matcher.group(1);
							String value = hashExternalParameters.get(key).getValue();

							// If type is "int", cast the value to int before inserting it.
							// TODO: clean this up
							if ("int".equals(type)) {
								updatedLine = "const " + type + " " + key + " = " + (Integer.parseInt(value)) + ";";
							} else {
								updatedLine = "const " + type + " " + key + " = " + value + ";";
							}
							break; // Stop checking keys for this line once a match is found.
						}
					}
				}
				if (hashInternalParameters.keySet().size() > 0) {
					// currently only works with doubles and ints
					for (String key : hashInternalParameters.keySet()) {
						String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(key) + "\\s*(=\\s*[^;]+)?;";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(line);
						if (matcher.find()) {
							String type = matcher.group(1);
							InternalParameter ip = hashInternalParameters.get(key);
							updatedLine = "const " + type + " " + key + " = " + ip.getValue() + ";";
							break; // Stop checking keys for this line once a match is found.
						}
					}
				}

				updatedContent.append(updatedLine).append(System.lineSeparator());
			}

			// System.out.println(path + "\n" + updatedContent.toString());
			// Write the updated content back to the file
			Files.write(path, updatedContent.toString().getBytes());
		} catch (IOException e) {
			System.err.println("Error updating model file: " + e.getMessage());
		}
	}

	public static void writeParametersToFile(String filePath, HashMap<String, IParameter> modelParameters) {

		try {
			// Read all lines from the file
			Path path = Paths.get(filePath);
			StringBuilder updatedContent = new StringBuilder();

			// Process each line from the file
			for (String line : Files.readAllLines(path)) {
				String updatedLine = line;

				// For each key in the constants map, use regex to find a match for "const
				// <type> key;"

				if (modelParameters.keySet().size() > 0) {
					for (String key : modelParameters.keySet()) {
						// Updated regex to match "const <type> key = <value>;" or "const <type> key;"
						String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(key) + "\\s*(=\\s*[^;]+)?;";
						Pattern pattern = Pattern.compile(regex);
						Matcher matcher = pattern.matcher(line);
						if (matcher.find()) {
							String modelConstantDataType = matcher.group(1);
							String value = modelParameters.get(key).getValue();

							// If type is "int", cast the value to int before inserting it.
							// TODO: clean this up
							if ("int".equals(modelConstantDataType)) {
								updatedLine = "const " + modelConstantDataType + " " + key + " = "
										+ (Integer.parseInt(value)) + ";";
							} else {
								updatedLine = "const " + modelConstantDataType + " " + key + " = " + value + ";";
							}
							break; // Stop checking keys for this line once a match is found.
						}
					}
				}

				updatedContent.append(updatedLine).append(System.lineSeparator());
			}

			// System.out.println(path + "\n" + updatedContent.toString());
			// Write the updated content back to the file
			Files.write(path, updatedContent.toString().getBytes());
		} catch (IOException e) {
			System.err.println("Error updating model file: " + e.getMessage());
		}
	}

	private static String findAndFillEvolvableParameter(String line, List<InternalParameter> internalParameters) {
		String updatedLine = null;
		for (InternalParameter ip : internalParameters) {
			// Updated regex to match "const <type> key = <value>;" or "const <type> key;"
			String regex = "const\\s+(\\S+)\\s+" + Pattern.quote(ip.getName()) + "\\s*(=\\s*[^;]+)?;";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String valueType = matcher.group(1);
				updatedLine = "evolve " + valueType + " " + ip.getName() + " [" + ip.getMin() + ".." + ip.getMax()
						+ "];";
				break;
			}
		}
		return updatedLine;
	}

	private static String findAndFillDummyParameter(String line) {
		String updatedLine = line;
		// Updated regex to match "const <type> key = <value>;" or "const <type> key;"
		String regex = "^const\\s+(\\w+)\\s+(\\w+)\\s*;\\s*(?:\\/\\/.*)?$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(line);
		if (matcher.find()) {
			String valueType = matcher.group(1);
			String parameterName = matcher.group(2);
			updatedLine = "const " + valueType + " " + parameterName + "=0;";
		}

		return updatedLine;
	}

	public static void writeEvolvablesToFile(String filePath,
			List<InternalParameter> internalParameters) {

		try {
			// Read all lines from the file
			Path path = Paths.get(filePath);
			StringBuilder updatedContent = new StringBuilder();

			// Process each line from the file
			for (String line : Files.readAllLines(path)) {

				String ipUpdate = findAndFillEvolvableParameter(line, internalParameters);
				if (ipUpdate != null) {
					updatedContent.append(ipUpdate).append(System.lineSeparator());
					continue;
				}

				updatedContent.append(line).append(System.lineSeparator());
			}

			// System.out.println(path + "\n" + updatedContent.toString());
			// Write the updated content back to the file
			Files.write(path, updatedContent.toString().getBytes());
		} catch (IOException e) {
			System.err.println("Error updating model file: " + e.getMessage());
		}
	}

	// we need this because EvoChecker requires the dependencies to have a value,
	// even though it doesn't actually do anything with them
	public static void writeDummyDependenciesToFile(String filePath) {

		try {
			// Read all lines from the file
			Path path = Paths.get(filePath);
			StringBuilder updatedContent = new StringBuilder();

			// Process each line from the file
			for (String line : Files.readAllLines(path)) {

				String updatedLine = findAndFillDummyParameter(line);
				updatedContent.append(updatedLine).append(System.lineSeparator());

			}

			// System.out.println(path + "\n" + updatedContent.toString());
			// Write the updated content back to the file
			Files.write(path, updatedContent.toString().getBytes());
		} catch (IOException e) {
			System.err.println("Error updating model file: " + e.getMessage());
		}
	}

	/**
	 * Reads and returns the first line of a file
	 * 
	 * @param filePath The path to the file
	 * @return The first line of the file, or null if the file couldn't be read
	 * @throws IOException If there's an error reading the file
	 */
	public static String readFirstLine(String filePath) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			return reader.readLine();
		}
	}

	/**
	 * Reads the entire contents of a file as a string
	 * 
	 * @param filePath The path to the file
	 * @return The contents of the file as a string, or null if the file couldn't be
	 *         read
	 * @throws IOException If there's an error reading the file
	 */
	public static String readFileAsString(String filePath) throws IOException {
		StringBuilder contentBuilder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				contentBuilder.append(line);
				contentBuilder.append(System.lineSeparator());
			}
		}
		return contentBuilder.toString();
	}
}
