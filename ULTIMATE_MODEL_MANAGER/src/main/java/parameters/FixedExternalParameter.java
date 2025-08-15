package parameters;

import java.io.IOException;

public class FixedExternalParameter extends ExternalParameter {

	private String value;

	public FixedExternalParameter(String name, String value) {
		super(name, null);
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

		return "Fixed external Parameter: " + super.getName() + "\nType: " + super.getName() + "\nValue: "
				+ super.getName()
				+ "\n";

	}
}
