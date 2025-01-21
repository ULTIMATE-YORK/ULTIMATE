package verification_engine.prism;

import prism.PrismDevNullLog;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.Result;
import prism.Prism;

import java.io.File;
import java.io.FileNotFoundException;

import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;

public class PrismAPI {
	
	public PrismAPI() {
		
	}
	
	public static void run(String modelFile, String propertyFile) throws FileNotFoundException, PrismException {
		
		// Create a log for PRISM output (hidden or stdout)
		//PrismLog mainLog = new PrismDevNullLog();
		PrismLog mainLog = new PrismFileLog("stdout");

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

}
