package ASTNodes;

public class BinaryExpressionAST extends BaseAST {
    private BaseAST left;
    private BaseAST right;
    private char operation;

    public BinaryExpressionAST(BaseAST left, BaseAST right, char operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    public BaseAST getLeft() {
        return left;
    }

    public void setLeft(BaseAST left) {
        this.left = left;
    }

    public BaseAST getRight() {
        return right;
    }

    public void setRight(BaseAST right) {
        this.right = right;
    }

    public char getOperation() {
        return operation;
    }

    public void setOperation(char operation) {
        this.operation = operation;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        left.accept(visitor);
        right.accept(visitor);
        visitor.visit(this);
    }
}
