package parameters;

public class FixedExternalParameter extends ExternalParameter {

	private String value;

	public FixedExternalParameter(String name, String value, String uniqueIdentifier) {
		super(name, uniqueIdentifier);
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	};

	@Override
	public void setValue(String value) {
		this.value = value;
	};

	public String toString() {

		return "Fixed External Parameter: " + super.getNameInModel() + "\nType: fixed" + "\nValue: "
				+ value + "\n";

	}
}
