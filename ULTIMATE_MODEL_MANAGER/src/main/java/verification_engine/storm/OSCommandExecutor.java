package verification_engine.storm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import model.persistent_objects.SharedData;

public class OSCommandExecutor {

    public static void executeCommand(String command) {
    	
    	String shell = "";
    	String flag = "";
    	
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
        	shell = "cmd.exe";
        	flag = "/c";
        }
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
        	shell = "/bin/sh";
        	flag = "-c";
        }
        
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(shell, flag, command);
            processBuilder.redirectErrorStream(true);  // Combine stdout and stderr

            Process process = processBuilder.start();

            // Create a file writer to write output to logs.txt
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                 BufferedWriter writer = new BufferedWriter(new FileWriter("logs.txt"))) {

                String line;
                while ((line = reader.readLine()) != null) {
                	System.out.println(line);
                    //writer.write(line);
                    //writer.newLine();  // Ensure each line is properly formatted
                }
            }

            int exitCode = process.waitFor();
            // update the logs via tab2
    		//SharedData context = SharedData.getInstance();
    		//context.getTab2Controller().updateLogs();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
