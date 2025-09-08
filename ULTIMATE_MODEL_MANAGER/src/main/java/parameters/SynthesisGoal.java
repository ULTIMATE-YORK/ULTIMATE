package parameters;

public class SynthesisGoal {

    private String definition;

    public SynthesisGoal(String definition) {
        this.definition = definition;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getDisplayString() {
        return "Synthesis objective: " + this.definition;
    }

    public String toString() {
        return this.definition;
    }

    public String getType(){
        if (definition.toLowerCase().startsWith("objective")){
            return "objective";
        } else if (definition.toLowerCase().startsWith("constraint")){
            return "constraint";
        } else {
            return "Unknown type for definition: " + definition;
        }
    }

}
