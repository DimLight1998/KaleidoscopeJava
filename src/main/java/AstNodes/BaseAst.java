package AstNodes;

public class BaseAst {
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }
}
