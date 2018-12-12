package ASTNodes

class BinaryExpressionAST(var left: BaseAST?, var right: BaseAST?, var operation: Char) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        left!!.accept(visitor)
        right!!.accept(visitor)
        visitor.visit(this)
    }
}
