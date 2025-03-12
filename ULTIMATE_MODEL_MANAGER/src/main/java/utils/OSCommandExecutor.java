package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility class for executing OS commands and capturing their output
 */
public class OSCommandExecutor {
    
    /**
     * Execute a command on the operating system and return the output
     * 
     * @param command The command to execute
     * @return The output of the command (stdout and stderr combined)
     */
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        try {
            // Execute the command
            process = Runtime.getRuntime().exec(command);
            
            // Read the output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Read any errors
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append("ERROR: ").append(line).append("\n");
                }
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Process exited with code ").append(exitCode).append("\n");
            }
            
        } catch (IOException e) {
            output.append("Error executing command: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        } catch (InterruptedException e) {
            output.append("Command execution interrupted: ").append(e.getMessage()).append("\n");
            Thread.currentThread().interrupt(); // Restore interrupted state
            e.printStackTrace();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        
        return output.toString();
    }
    
    /**
     * Execute a command on the operating system with a timeout
     * 
     * @param command The command to execute
     * @param timeoutSeconds Maximum time to wait for the command to complete (in seconds)
     * @return The output of the command (stdout and stderr combined)
     */
    public static String executeCommand(String command, int timeoutSeconds) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        try {
            // Execute the command
            process = Runtime.getRuntime().exec(command);
            
            // Read the output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Read any errors
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append("ERROR: ").append(line).append("\n");
                }
            }
            
            // Wait for the process to complete with timeout
            boolean completed = process.waitFor(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
            
            if (!completed) {
                process.destroy();
                output.append("Process timed out after ").append(timeoutSeconds).append(" seconds\n");
            } else {
                int exitCode = process.exitValue();
                if (exitCode != 0) {
                    output.append("Process exited with code ").append(exitCode).append("\n");
                }
            }
            
        } catch (IOException e) {
            output.append("Error executing command: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        } catch (InterruptedException e) {
            output.append("Command execution interrupted: ").append(e.getMessage()).append("\n");
            Thread.currentThread().interrupt(); // Restore interrupted state
            e.printStackTrace();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        
        return output.toString();
    }
    
    /**
     * Execute a command on the operating system with arguments provided as an array
     * 
     * @param commandArray The command and its arguments as an array
     * @return The output of the command (stdout and stderr combined)
     */
    public static String executeCommandArray(String[] commandArray) {
        StringBuilder output = new StringBuilder();
        Process process = null;
        try {
            // Execute the command
            process = Runtime.getRuntime().exec(commandArray);
            
            // Read the output
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            
            // Read any errors
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append("ERROR: ").append(line).append("\n");
                }
            }
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Process exited with code ").append(exitCode).append("\n");
            }
            
        } catch (IOException e) {
            output.append("Error executing command: ").append(e.getMessage()).append("\n");
            e.printStackTrace();
        } catch (InterruptedException e) {
            output.append("Command execution interrupted: ").append(e.getMessage()).append("\n");
            Thread.currentThread().interrupt(); // Restore interrupted state
            e.printStackTrace();
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
        
        return output.toString();
    }
}