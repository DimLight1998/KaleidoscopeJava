package ASTNodes;

public class BaseAST {
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }
}
