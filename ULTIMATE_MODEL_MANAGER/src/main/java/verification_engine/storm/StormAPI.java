package verification_engine.storm;

import headless.StormOutputParser;
import model.persistent_objects.Model;

public class StormAPI {
	
	public static double run(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --prism " + model.getFilePath()  + " --prop \"" + propFile + "\"";
		String output = OSCommandExecutor.executeCommand(command);
		Double result = StormOutputParser.getDResult(output);
		return result;
	}
	
	public static void runPars(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --mode solutionfunction --prism " + model.getFilePath()  + " --prop \"" + propFile + "\"";
		OSCommandExecutor.executeCommand(command);
	}

}
