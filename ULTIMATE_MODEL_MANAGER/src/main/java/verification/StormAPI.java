package verification;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.Model;
import project.Project;
import sharedContext.SharedContext;

public class StormAPI {
	
    Project project;
    
	private static final Logger logger = LoggerFactory.getLogger(StormAPI.class);
	
	/**
	 * Run Storm model checker with proper handling for labels and named reward structures.
	 * 
	 * Supports:
	 * - Labels: P=?[F "success"]
	 * - Named rewards: R{"reward_name"}=?[F s=1]
	 * 
	 * @param model The model to verify
	 * @param propFile The property to check (can contain labels or named rewards)
	 * @return The verification result
	 * @throws IOException if execution fails
	 */
	public double run(Model model, String propFile) throws IOException {
    	project = SharedContext.getProject();
	    String si = project.getStormInstall();
	    
	    // Use single quotes for the property to avoid shell escaping issues with
	    // labels (e.g., "success") and named reward structures (e.g., {"name"})
	    // We need to escape any single quotes in the property by replacing ' with '\''
	    String escapedProp = propFile.replace("'", "'\\''");
	    
		String command = String.format(si + " --prism \"%s\" --prop '%s' -pc", 
		    model.getVerificationFilePath(), 
		    escapedProp);
		
		logger.info("Executing Storm command: " + command);
		String output = OSCommandExecutor.executeCommand(command);
		logger.info(output);
		Double result = StormOutputParser.getDResult(output);
		return result;
	}
	
	/**
	 * Run Storm parametric model checker with proper handling for labels and named reward structures.
	 * 
	 * @param model The model to verify
	 * @param propFile The property to check (can contain labels or named rewards)
	 * @return The parametric solution function as a string
	 * @throws IOException if execution fails
	 */
	public String runPars(Model model, String propFile) throws IOException {
    	project = SharedContext.getProject();
	    String spi = project.getStormParsInstall();
	    
	    // Use single quotes for the property to avoid shell escaping issues
	    String escapedProp = propFile.replace("'", "'\\''");
	    
		String command = String.format(spi + " --mode solutionfunction --prism \"%s\" --prop '%s'", 
		    model.getVerificationFilePath(), 
		    escapedProp);
		
		logger.info("Executing Storm-pars command: " + command);
		String output = OSCommandExecutor.executeCommand(command);
		logger.info(output);
		String result = StormOutputParser.getSResult(output);
		return result;
	}
}
