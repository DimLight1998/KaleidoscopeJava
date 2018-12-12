package ASTNodes;

import java.util.List;

public class FunctionCallAST extends BaseAST {
    private String functionName;
    private List<BaseAST> params;

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public List<BaseAST> getParams() {
        return params;
    }

    public void setParams(List<BaseAST> params) {
        this.params = params;
    }

    public FunctionCallAST(String functionName, List<BaseAST> params) {
        this.functionName = functionName;
        this.params = params;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        for (BaseAST ast : params) {
            ast.accept(visitor);
        }
        visitor.visit(this);
    }
}
