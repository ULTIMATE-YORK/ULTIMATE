package verification;

import model.Model;

public class StormAPI {
	
	public static double run(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --prism " + model.getFilePath()  + " --prop " + "'" + propFile + "'" + " -pc";
		String output = OSCommandExecutor.executeCommand(command);
		System.out.print(output);
		Double result = StormOutputParser.getDResult(output);
		return result;
	}
	
	public static String runPars(Model model, String propFile, String stormInstallLocation) {
		String command = stormInstallLocation + " --mode solutionfunction --prism " + model.getFilePath()  + " --prop " + "'" + propFile + "'" + " -pc";
		String output = OSCommandExecutor.executeCommand(command);
		System.out.print(output);
		String result = StormOutputParser.getSResult(output);
		return result;
	}

}