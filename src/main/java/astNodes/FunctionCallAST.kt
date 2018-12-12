package astNodes

class FunctionCallAST(var functionName: String?, var params: List<BaseAST>?) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
