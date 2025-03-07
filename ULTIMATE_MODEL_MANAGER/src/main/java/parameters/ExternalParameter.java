package parameters;

import java.io.IOException;

import javafx.application.Platform;
import learning.BayesianAverageCalculator;
import learning.MeanCalculator;
import project.Project;
import sharedContext.SharedContext;
import utils.Alerter;

public class ExternalParameter {
    private String name;
    private String type;
    private String value;
    private double learnedValue;
    
    private SharedContext sharedContext = SharedContext.getInstance();
    private Project project = sharedContext.getProject();
    
    public ExternalParameter(String name, String type, String value) throws NumberFormatException, IOException {
        this.name = name;
        this.type = type;
        this.value = value;
        if (!verifyFile()) {
        	Platform.runLater(() -> Alerter.showErrorAlert("Could not create External Paramater", "The file format is invalid."));
            throw new IllegalArgumentException("Invalid file format!");
        }
        this.learnedValue = evaluate();
    }
    
    // GETTER METHODS
    
    public String getName() {
    	return this.name;	
    }
    
    public String getType() {
    	return this.type;
    }
    
    public String getValue() {
    	return this.value;
    }
    
	public double getLearnedValue() {
		return this.learnedValue;
	}
    
    // SETTER METHODS
    
    public void setName(String newName) {
    	this.name = newName;
    }
    
    public void setType(String newtype) {
    	this.type = newtype;
    }
    
    public void setValue(String newValue) {
    	this.value = newValue;
    }
    
    /*
     * This method can be called on an external parameter to retrieve the value regardless of whether it is learned or not
     * 
     * @return double
     */

	 // FIXME : os-dependant paths
	 public double evaluate() throws NumberFormatException, IOException {
	     switch (type) {
	         case "Fixed":
	             return Double.parseDouble(value);
	         case "Mean":
					// FIXME : os-dependant paths
	             return MeanCalculator.computeMean(project.directory() + "/" + value, "\n");
	         case "Mean-Rate":
					// FIXME : os-dependant paths
					return MeanCalculator.computeMeanRate(project.directory() + "/" + value, "\n");
			 case "Bayes":
				 return BayesianAverageCalculator.computeBayesianAverage(project.directory() + "/" + value);
		     case "Bayes-Rate":
		    	 return BayesianAverageCalculator.computeBayesianAverageRate(project.directory() + "/" + value, "\n");
	     }
	     return 0.0;
	 }
	 
	 // this method will ensure that the data file is a valid format based on type
	 private boolean verifyFile() {
			switch (type) {
			case "Fixed":
				return true;
			case "Mean":
				// if computeMean does not throw an exception, the file is valid for mean calculation
				try {
					// FIXME : os-dependant paths
					MeanCalculator.computeMean(project.directory() + "/" + value, "\n");
					return true;
				} catch (NumberFormatException e) {
					//Platform.runLater(() -> Alerter.showErrorAlert("Invalid file format!", e.getMessage()));
					return false;
				} catch (IOException e) {
					//Platform.runLater(() -> Alerter.showErrorAlert("File not found!", e.getMessage()));
					return false;
				}
			case "Mean-Rate":
				// if computeMeanRate does not throw an exception, the file is valid for
				// mean-rate calculation
				try {
					// FIXME : os-dependant paths
					MeanCalculator.computeMeanRate(project.directory() + "/" + value, "\n");
					return true;
				} catch (NumberFormatException e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("Invalid file format!",
					// e.getMessage()));
					return false;
				} catch (IOException e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("File not found!",
					// e.getMessage()));
					return false;
				}
			case "Bayes":
				// if computeBayesianAverage does not throw an exception, the file is valid for
				// bayesian average calculation
				try {
					// FIXME : os-dependant paths
					BayesianAverageCalculator.computeBayesianAverage(project.directory() + "/" + value);
					return true;
				} catch (NumberFormatException e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("Invalid file format!",
					// e.getMessage()));
					return false;
				} catch (IOException e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("File not found!",
					// e.getMessage()));
					return false;
				} catch (Exception e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("Invalid file format!",
					// e.getMessage()));
					return false;
				}
			case "Bayes-Rate":
				// if computeBayesianAverageRate does not throw an exception, the file is valid
				// for
				// bayesian average rate calculation
				try {
					// FIXME : os-dependant paths
					BayesianAverageCalculator.computeBayesianAverageRate(project.directory() + "/" + value, "\n");
					return true;
				} catch (NumberFormatException e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("Invalid file format!",
					// e.getMessage()));
					return false;
				} catch (IOException e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("File not found!",
					// e.getMessage()));
					return false;
				} catch (Exception e) {
					// Platform.runLater(() -> Alerter.showErrorAlert("Invalid file format!",
					// e.getMessage()));
					return false;
				}
			}
			return false;
	 }
    
    public String toString() {
    	try {
    		if (type.equals("Fixed")) {
            	return "External Parameter: " + name + "\nType: " + type + "\nValue: " + Double.toString(learnedValue) + "\n";
    		}
    		else {
            	return "External Parameter: " + name + "\nType: " + type + "\nValue: " + Double.toString(learnedValue) + "\nSource: " + value + "\n";
    		}
    	} finally {}
    }
}
