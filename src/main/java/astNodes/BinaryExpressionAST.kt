package astNodes

class BinaryExpressionAST(var left: BaseAST, var right: BaseAST, var operation: Char) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
