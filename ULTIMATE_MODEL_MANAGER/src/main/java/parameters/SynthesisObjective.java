package parameters;

public class SynthesisObjective {

    private String definition;

    public SynthesisObjective(String definition) {
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
        return "Synthesis objective: " + this.definition;
    }

}
