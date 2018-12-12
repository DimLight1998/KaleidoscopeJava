package AstNodes;

public class FunctionDefAst extends BaseAst {
    private FunctionProtoAst prototype;
    private BaseAst body;

    public FunctionDefAst(FunctionProtoAst prototype, BaseAst body) {
        this.prototype = prototype;
        this.body = body;
    }

    public FunctionProtoAst getPrototype() {
        return prototype;
    }

    public void setPrototype(FunctionProtoAst prototype) {
        this.prototype = prototype;
    }

    public BaseAst getBody() {
        return body;
    }

    public void setBody(BaseAst body) {
        this.body = body;
    }

    @Override
    public void accept(AstVisitor visitor) {
        prototype.accept(visitor);
        body.accept(visitor);
        visitor.visit(this);
    }
}
