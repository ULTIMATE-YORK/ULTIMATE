package parameters;

public abstract class ExternalParameter implements IStaticParameter {
	private String name;
	private final String uniqueIdentifier;

	public ExternalParameter(String name, String uniqueIdentifier) {
		this.name = name;
		this.uniqueIdentifier = uniqueIdentifier;
	}

	public void setName(String newName) {
		this.name = newName;
	}

	public String getNameInModel() {
		return this.name;
	}

	// public String getNameInModel(){
	// 	return uniqueIdentifier;
	// }

	public abstract String toString();

	public abstract void setValue(String value) throws Exception;

}
