package AstNodes;

import java.util.List;

public class FunctionCallAst extends BaseAst{
    private String functionName;
    private List<BaseAst> params;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public FunctionCallAst(String functionName, List<BaseAst> params) {
        this.functionName = functionName;
        this.params = params;
    }
}
