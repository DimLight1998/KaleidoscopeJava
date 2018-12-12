package AstNodes;

public class VariableAst extends BaseAst {
    private String variableName;

    public VariableAst(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }
}
