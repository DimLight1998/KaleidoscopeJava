package astNodes

class ForExpressionAST(
        var varName: String,
        var start: BaseAST,
        var end: BaseAST,
        var step: BaseAST,
        var body: BaseAST
) : BaseAST() {
    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}