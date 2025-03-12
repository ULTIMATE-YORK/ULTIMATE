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

public class PrismAPI {
	
	private static final Logger logger = LoggerFactory.getLogger(PrismAPI.class);
		
	public static void run(String modelFile, String propertyFile) throws FileNotFoundException, PrismException {
		
		// Create a log for PRISM output (hidden or stdout)
		// PrismLog mainLog = new PrismDevNullLog();
		PrismLog mainLog = new PrismFileLog("logs.txt");

		// Initialise PRISM engine 
		Prism prism = new Prism(mainLog);
		prism.initialise();
		
		// Parse and load a PRISM model from a file
		ModulesFile modulesFile = prism.parseModelFile(new File(modelFile));
		prism.loadPRISMModel(modulesFile);

		// Parse and load a properties model for the model
		PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propertyFile));
				
		for (int i=0; i< propertiesFile.getNumProperties(); i++) {
			// Model check property from the file
			Result result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(i));
			System.out.println(result.getResult());
		}
	}
	
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
			for (int i=0; i< propertiesFile.getNumProperties(); i++) {
				// Model check property from the file
				result = prism.modelCheck(propertiesFile, propertiesFile.getPropertyObject(i));
			}
		}
				
		// update the logs tab
		mainLog.flush();
		return (Double) result.getResult();
	}
	
	
}