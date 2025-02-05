package verification_engine.storm;

import model.persistent_objects.Model;

public class StormAPI {
	
	public static void run(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --prism " + model.getFilePath()  + " --prop \"" + propFile + "\"";
		OSCommandExecutor.executeCommand(command);
	}
	
	public static void runPars(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --mode solutionfunction --prism " + model.getFilePath()  + " --prop \"" + propFile + "\"";
		OSCommandExecutor.executeCommand(command);
	}

}
