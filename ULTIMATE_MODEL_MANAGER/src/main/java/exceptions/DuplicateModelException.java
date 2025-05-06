package exceptions;

public class DuplicateModelException extends RuntimeException {
    public DuplicateModelException(Model duplicateModel) {
        super("Model " + duplicateModel.getModelId() + " is already in the project!");
    }
}

