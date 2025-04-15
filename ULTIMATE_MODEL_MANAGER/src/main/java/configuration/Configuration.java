package configuration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.zip.DataFormatException;

import org.json.JSONObject;

public class Configuration {
	
	private static Configuration instance = null;
	public static String PRISM_PATH;
	public static String PRISM_GAMES_PATH;
	public static String STORM_PATH;
	public static String STORM_PARS_PATH;
	public static String PYTHON_PATH;
	
	private Configuration() throws IOException, DataFormatException {
		if (isConfigFile() && areValidPaths()) {
            setPaths();
        }
	}
	
	public static void init() throws FileNotFoundException, IOException, DataFormatException {
		if (instance == null) {
			instance = new Configuration();
		}	
	}
	
	/*
	 * This method returns the 5 paths to the binaries
	 * 
	 * @return a map of the name of the binary and the path to it
	 * @throws DataFormatException if the file is not in the correct format
	 */
	private HashMap<String, String> getPaths() throws IOException, DataFormatException {
        File configFile = new File("config.json");
        String content = new String(Files.readAllBytes(Paths.get(configFile.toURI())));
        JSONObject configJSON = new JSONObject(content);
        HashMap<String, String> binaries = new HashMap<String, String>();
        try {
        	binaries.put("PRISM_PATH", configJSON.getString("prismInstall"));
        	binaries.put("PRISM_GAMES_PATH", configJSON.getString("prismGamesInstall"));
        	binaries.put("STORM_PATH", configJSON.getString("stormInstall"));
        	binaries.put("STORM_PARS_PATH", configJSON.getString("stormParsInstall"));
        	binaries.put("PYTHON_PATH", configJSON.getString("pythonInstall"));
		} catch (Exception e) {
			throw new DataFormatException("Configuration file is not in the correct format");
		}
        return binaries;
	}
	
	/*
	 * This method verifies that config.json exists where it is expected to be
	 * 
	 * @return true if config.json exists, false otherwise
	 * @throws FileNotFoundException if config.json does not exist
	 */
	private boolean isConfigFile() throws FileNotFoundException {
		boolean exists;
		exists = Files.exists(Paths.get("config.json"));
		if (exists) {
			return true;
		} else {
			throw new FileNotFoundException("Configuration file not found! This file is expected in directory where the code is being run");
		}
	}
	
	/*
	 * This method verifies that the paths in config.json are valid binaries on the system
	 * 
	 * @return true if the paths are valid, false otherwise
	 * @throws FileNotFoundException if the paths are not valid binaries
	 */
	private boolean areValidPaths() throws IOException, DataFormatException {
		// get the paths from the config.json file
		HashMap<String, String> binaries = getPaths();
		// check if the paths are valid
		for (String key : binaries.keySet()) {
			if (!Files.isExecutable(Paths.get(binaries.get(key)))) {
				throw new DataFormatException("Path to " + key + " is not valid");
			}
		}
		return true;
	}
	
	private void setPaths() {
		HashMap<String, String> binaries = null;
        try {
            binaries = getPaths();
        } catch (IOException | DataFormatException e) {
            e.printStackTrace();
        }
        PRISM_PATH = binaries.get("PRISM_PATH");
        PRISM_GAMES_PATH = binaries.get("PRISM_GAMES_PATH");
        STORM_PATH = binaries.get("STORM_PATH");
        STORM_PARS_PATH = binaries.get("STORM_PARS_PATH");
        PYTHON_PATH = binaries.get("PYTHON_PATH");
    }

}
