package verification_engine.storm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSCommandExecutor {

    public static void executeCommand(String command) {
    	
    	String shell = "";
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
        	shell = "cmd.exe";
        }
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
        	shell = "/bin/sh";
        }
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(shell, "-c", command);
            processBuilder.redirectErrorStream(true);  // Combine stdout and stderr

            Process process = processBuilder.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    System.out.flush();  // Ensure output is displayed immediately
                }
            }

            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
