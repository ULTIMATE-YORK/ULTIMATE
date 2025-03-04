package verification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

public class OSCommandExecutor {

    public static String executeCommand(String command) {
    	
    	String shell = "";
    	String flag = "";
        String line = "";

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
            	
            	String readLine;
                while ((readLine = reader.readLine()) != null) {
                	line += readLine;
                	line += "\n";
                	//writer.write(line);
                    //writer.newLine();  // Ensure each line is properly formatted
                }
            }
        	
            //System.out.println(line);
        	//System.out.println(StormOutputParser.getSResult(line).toString());
            
        	
        	int exitCode = process.waitFor();
            // update the logs via tab2
    		//SharedData context = SharedData.getInstance();
    		//context.getTab2Controller().updateLogs();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        
        return line;
    }
}