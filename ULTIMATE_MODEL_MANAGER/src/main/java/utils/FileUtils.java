package utils;

import java.nio.file.Files;
import java.nio.file.Paths;

public class FileUtils {
	
	/**
	 * Check if a file is an existing prism file by checking the file exists and ends with a prism model file extension
	 * 
	 * @param filePath
	 * @return boolean
	 */
	public static boolean isPrismFile(String filePath) {
		return (isFile(filePath) && isPrismModelFile(filePath));
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
