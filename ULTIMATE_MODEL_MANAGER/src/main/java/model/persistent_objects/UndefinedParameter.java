package model.persistent_objects;

/**
 * The UndefinedParameter class represents a parameter with an undefined or arbitrary name.
 * It extends the Parameter class.
 */
public class UndefinedParameter extends Parameter {

 /**
  * The name of the parameter.
  */
 private String parameter;

 /**
  * Constructs an UndefinedParameter with the specified name.
  *
  * @param parameter the name of the parameter
  */
 public UndefinedParameter(String parameter) {
  this.parameter = parameter;
 }

 /**
  * Returns the name of the parameter.
  *
  * @return the name of the parameter
  */
 public String getName() {
  return this.parameter;
 }

 /**
  * Sets the name of the parameter.
  *
  * @param newName the new name of the parameter
  */
 public void setName(String newName) {
  this.parameter = newName;
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
