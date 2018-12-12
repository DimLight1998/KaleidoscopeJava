package ASTNodes

class NumberAST(var value: Double) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
