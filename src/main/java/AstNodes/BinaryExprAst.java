package AstNodes;

public class BinaryExprAst extends BaseAst{
    private BaseAst left;
    private BaseAst right;
    private char operation;

    public BinaryExprAst(BaseAst left, BaseAst right, char operation) {
        this.left = left;
        this.right = right;
        this.operation = operation;
    }

    public BaseAst getLeft() {
        return left;
    }

    public void setLeft(BaseAst left) {
        this.left = left;
    }

    public BaseAst getRight() {
        return right;
    }

    public void setRight(BaseAst right) {
        this.right = right;
    }

    public char getOperation() {
        return operation;
    }

    public void setOperation(char operation) {
        this.operation = operation;
    }
}
