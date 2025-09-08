package synthesis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SynthesisExport {

    public static void createPermanentCopy(String filePathString, String newPathString) throws IOException {

        Path tempPath = Path.of(filePathString);
        Path newPath = Path.of(newPathString);

        Files.copy(tempPath, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

    }

}
