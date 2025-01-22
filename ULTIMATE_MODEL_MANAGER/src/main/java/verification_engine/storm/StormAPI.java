package verification_engine.storm;

import java.util.List;

import model.persistent_objects.Model;
import model.persistent_objects.Property;

public class StormAPI {
	
	public static void run(Model model) {
		
	}
	
	public static void run(Model model, Property prop) {
		
	}
	
	public static void run(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --prism " + model.getFilePath() + " --prop " + propFile;
		OSCommandExecutor.executeCommand(command);
	}
	
	public static void run(List<Model> models) {
		
	}

}
