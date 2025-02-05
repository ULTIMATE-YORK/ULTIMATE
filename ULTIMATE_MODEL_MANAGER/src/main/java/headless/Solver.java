package headless;

import java.io.File;
import java.io.FileNotFoundException;

import model.persistent_objects.Model;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLog;
import prism.Result;

public class Solver {
	
	private String stormInstallation;
	
	public Solver() {
		
	}
	
	public Solver(String stormInstallation) {
		this.stormInstallation = stormInstallation;
	}
	
	public static double prism(Model model, String property) throws FileNotFoundException, PrismException {
		
		boolean singleProperty = false;
		if (property.contains("[")) {
			singleProperty = true;
		}
		
		// Create a log for PRISM output (hidden or stdout)
		//PrismLog mainLog = new PrismDevNullLog();
		PrismLog mainLog = new PrismFileLog("stdout");

		// Initialise PRISM engine 
		Prism prism = new Prism(mainLog);
		prism.initialise();
		
		// Parse and load a PRISM model from a file
		ModulesFile modulesFile = prism.parseModelFile(new File(model.getFilePath()));
		prism.loadPRISMModel(modulesFile);
		
		PropertiesFile propertiesFile;
		Result result = null;
		
		if (singleProperty) {
			propertiesFile = prism.parsePropertiesString(property);
			result = prism.modelCheck(property);
		}
		else {
			// Parse and load a properties model for the model
			propertiesFile = prism.parsePropertiesFile(modulesFile, new File(property));
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
