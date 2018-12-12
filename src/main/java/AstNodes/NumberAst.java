package AstNodes;

public class NumberAst  extends BaseAst{
    private double value;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public NumberAst(double value) {
        this.value = value;
    }
}
