package exceptions;

import java.nio.file.Path;

public class InvalidPrismFile extends RuntimeException {
	
	public InvalidPrismFile(Path prismFile) {
		super("The Prism file " + prismFile.toString() + " is invalid.");
	}
}
