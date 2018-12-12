package AstNodes;

import java.util.List;

public class FunctionCallAst extends BaseAst {
    private String functionName;
    private List<BaseAst> params;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<BaseAst> getParams() {
        return params;
    }

    public void setParams(List<BaseAst> params) {
        this.params = params;
    }

    public FunctionCallAst(String functionName, List<BaseAst> params) {
        this.functionName = functionName;
        this.params = params;
    }

    @Override
    public void accept(AstVisitor visitor) {
        for (BaseAst ast : params) {
            ast.accept(visitor);
        }
        visitor.visit(this);
    }
}
