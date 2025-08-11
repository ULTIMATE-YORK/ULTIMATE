package verification;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import project.Project;
import sharedContext.SharedContext;

public class StormAPI {
	
    private SharedContext sharedContext = SharedContext.getContext();
    Project project;
    
	private static final Logger logger = LoggerFactory.getLogger(StormAPI.class);
	
	public double run(Model model, String propFile) throws IOException {
    	project = SharedContext.getUltimateInstance().getProject();
	    String si = project.getStormInstall();
		String command = String.format(si + " --prism \"%s\" --prop \"%s\" -pc", model.getVerificationFilePath(), propFile);
		String output = OSCommandExecutor.executeCommand(command);
		logger.info(output);
		Double result = StormOutputParser.getDResult(output);
		return result;
	}
	
	public String runPars(Model model, String propFile) throws IOException {
    	project = SharedContext.getUltimateInstance().getProject();
	    String spi = project.getStormParsInstall();
		String command = String.format(spi + " --mode solutionfunction --prism \"%s\" --prop \"%s\"", model.getVerificationFilePath(), propFile);
		String output = OSCommandExecutor.executeCommand(command);
		logger.info(output);
		String result = StormOutputParser.getSResult(output);
		return result;
	}

}