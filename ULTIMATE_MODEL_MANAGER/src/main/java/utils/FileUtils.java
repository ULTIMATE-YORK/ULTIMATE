package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtils {
	
	/**
	 * Check if a file is an existing prism file by checking the file exists and ends with a prism model file extension
	 * 
	 * @param filePath
	 * @return boolean
	 * @throws IOException 
	 */
	public static boolean isPrismFile(String filePath) throws IOException {
		if (isFile(filePath) && isPrismModelFile(filePath)) {
			return true;
		}
		else {
			throw new IOException("File at " + filePath + " does not exist or is not a prism model file");
		}
	}
	
	/*
	 * Check if a file is an existing ultimate file by checking the file extension
	 * 
	 * @param projectPath
	 * @return boolean
	 */
	public static boolean isUltimateFile(String projectPath) throws IOException {
        if (isFile(projectPath) && projectPath.toLowerCase().endsWith(".ultimate")) {
            return true;
        }
        else {
            throw new IOException("File does not exist or is not an ultimate file");
        }
	}
	
	/**
	 * Check if a file is an existing file
	 * 
	 * @param filePath
	 * @return boolean
	 */
	public static boolean isFile(String filePath) {
		// check file exists
		return Files.exists(Paths.get(filePath));
	}
	
	/*
	 * Returns the name of a file without the file extension
	 * @param filePath
	 * @return String the file name without the file extension
	 */
	public static String removePrismFileExtension(String filePath) throws IOException {
		if (isPrismFile(filePath)) {
	        Path path = Paths.get(filePath);
	        String fileName = path.getFileName().toString(); // Get "c.prism"
	        
	        int lastDotIndex = fileName.lastIndexOf('.');
	        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
		}
		return null;
	}
	
	/*
	 * Returns the name of a file without the file extension
	 * @param filePath
	 * @return String the file name without the file extension
	 */
	public static String removeUltimateFileExtension(String filePath) throws IOException {
		if (isUltimateFile(filePath)) {
	        Path path = Paths.get(filePath);
	        String fileName = path.getFileName().toString(); // Get "c.prism"
	        
	        int lastDotIndex = fileName.lastIndexOf('.');
	        return (lastDotIndex == -1) ? fileName : fileName.substring(0, lastDotIndex);
		}
		return null;
	}
	
	// PRIVATE METHODS
	
	/*
	 * Check if a file is an existing prism model file by checking the file extension
	 * 
	 * @param filePath
	 * @return boolean
	 */
	private static boolean isPrismModelFile(String filePath) {
		String[] extensions = new String[] {".prism", ".ctmc", ".dtmc", ".mdp", ".pomdp"};
        for (String ext : extensions) {
            if (filePath.toLowerCase().endsWith(ext.toLowerCase())) {
                return true;
            }
        }
        return false;
	}
	
}
