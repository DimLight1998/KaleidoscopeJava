package AstNodes;

import java.util.List;

public class FunctionProtoAst extends BaseAst {
    private String functionName;
    private List<String> params;

    public FunctionProtoAst(String functionName, List<String> params) {
        this.functionName = functionName;
        this.params = params;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }
}
