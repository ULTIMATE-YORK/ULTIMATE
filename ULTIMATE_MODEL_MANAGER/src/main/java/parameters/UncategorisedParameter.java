package parameters;

/**
 * The UncategorisedParameter class represents a parameter with an undefined or arbitrary name.
 * It extends the Parameter class - note: no it doesn't...
 */
public class UncategorisedParameter {

	 /**
	  * The name of the parameter.
	  */
	 private String name;
	
	 /**
	  * Constructs an UndefinedParameter with the specified name.
	  *
	  * @param name the name of the parameter
	  */
	 public UncategorisedParameter(String name) {
	  this.name = name;
	 }
	
	 /**
	  * Returns the name of the parameter.
	  *
	  * @return the name of the parameter
	  */
	 public String getName() {
	  return this.name;
	 }
	
	 /**
	  * Sets the name of the parameter.
	  *
	  * @param newName the new name of the parameter
	  */
	 public void setName(String newName) {
	  this.name = newName;
	 }
	
	 /**
	  * Returns a string representation of the parameter.
	  *
	  * @return the name of the parameter
	  */
	 public String toString() {
	  return getName();
	 }
}
