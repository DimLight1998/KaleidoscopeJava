package ASTNodes;

public class FunctionDefinitionAST extends BaseAST {
    private FunctionPrototypeAST prototype;
    private BaseAST body;

    public FunctionDefinitionAST(FunctionPrototypeAST prototype, BaseAST body) {
        this.prototype = prototype;
        this.body = body;
    }

    public FunctionPrototypeAST getPrototype() {
        return prototype;
    }

    public void setPrototype(FunctionPrototypeAST prototype) {
        this.prototype = prototype;
    }

    public BaseAST getBody() {
        return body;
    }

    public void setBody(BaseAST body) {
        this.body = body;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        prototype.accept(visitor);
        body.accept(visitor);
        visitor.visit(this);
    }
}
