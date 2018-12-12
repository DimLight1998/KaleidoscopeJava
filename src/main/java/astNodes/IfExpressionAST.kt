package astNodes

class IfExpressionAST(
        var condition: BaseAST,
        var positive: BaseAST,
        var negative: BaseAST
) : BaseAST() {
    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}