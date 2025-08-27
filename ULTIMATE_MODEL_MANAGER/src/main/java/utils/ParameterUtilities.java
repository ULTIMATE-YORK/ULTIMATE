package utils;

public class ParameterUtilities {

    public static String generateUniqueParameterId(String modelId, String paramName) {
        return modelId + "-" + paramName;
    }

}