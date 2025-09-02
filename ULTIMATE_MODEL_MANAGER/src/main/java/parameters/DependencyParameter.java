package parameters;

import model.Model;

public class DependencyParameter implements IParameter {
    private String name;
    private Model sourceModel;
    private String definition; // definition of the property to be verified on model
    private String uniqueIdentifier;
    private String result;

    public DependencyParameter(String name, Model model, String definition, String uniqueIdentifier) {
        this.name = name;
        this.sourceModel = model;
        this.definition = definition;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    // GETTER METHODS

    // public String getNameInModel() {
    // return uniqueIdentifier;
    // }

    public String getNameInModel() {
        return this.name;
    }

    public Model getSourceModel() {
        return this.sourceModel;
    }

    public String getDefinition() {
        return this.definition;
    }

    public String getValue() {
        return this.result;
    }

    // SETTER METHODS

    public void setName(String newName) {
        this.name = newName;
    }

    public void setSourceModel(Model newModel) {
        this.sourceModel = newModel;
    }

    public void setDefinition(String newDefinition) {
        this.definition = newDefinition;
    }

    public void setValue(String newResult) {
        this.result = newResult;
    }

    public String getType() {
        return "dependency";
    }

    public String toString() {
        return "Dependency Parameter: " + name + "\nModel ID: " + sourceModel.getModelId() + "\nProperty Definition: "
                + definition.replace("\\", "") + "\n";
    }

    public boolean sanityCheck(String value) {

        Double parsedValue = Double.valueOf(value);

        if (definition.startsWith("P=?") || definition.startsWith("Pmin=?") || definition.startsWith("Pmax=?")) {
            if (parsedValue < 0 || parsedValue > 1) {
                return false;
            }
        }

        return true;

    }

}
