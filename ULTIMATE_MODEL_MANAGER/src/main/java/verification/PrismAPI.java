package verification;

import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.Result;
import prism.Prism;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.io.IOException;


public class PrismAPI {
	
	private static final Logger logger = LoggerFactory.getLogger(PrismAPI.class);
	
	/**
	 * 
	 * @param model
	 * @param propertyFile
	 * @param singleProperty Set this to false if propertyFile is a file path, true if a single property is passed
	 * @return
	 * @throws FileNotFoundException
	 * @throws PrismException
	 */
	public static double run(Model model, String propertyFile, boolean singleProperty) throws FileNotFoundException, PrismException {
	    
	    // Create a log for PRISM output (hidden or stdout)
	    //PrismLog mainLog = new PrismDevNullLog();
	    PrismLog mainLog = new PrismFileLog("logs.txt");

	    // Initialise PRISM engine 
	    Prism prism = new Prism(mainLog);
	    prism.initialise();
	    
	    // Parse and load a PRISM model from a file
	    ModulesFile modulesFile = prism.parseModelFile(new File(model.getFilePath()));
	    prism.loadPRISMModel(modulesFile);
	    
	    PropertiesFile propertiesFile;
	    Result result = null;
	    
	    if (singleProperty) {
	        propertiesFile = prism.parsePropertiesString(propertyFile);
	        result = prism.modelCheck(propertyFile);
	    }
	    else {
	        // Parse and load a properties model for the model
	        propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertyFile));
	        for (int i = 0; i < propertiesFile.getNumProperties(); i++) {
	            // Model check property from the file
	            result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(i));
	        }
	    }
	    
	    // Read the file logs.txt and log its content before flushing the log.
	    try {
	        String logsContent = new String(Files.readAllBytes(Paths.get("logs.txt")), StandardCharsets.UTF_8);
	        logger.info(logsContent);
	    } catch (IOException e) {
	        logger.error("Failed to read logs.txt", e);
	    }
	    
	    // Update the logs tab by flushing the log
	    mainLog.flush();
	    return (Double) result.getResult();
	}

}