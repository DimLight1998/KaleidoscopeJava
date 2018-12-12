package ASTNodes

class FunctionCallAST(var functionName: String?, var params: List<BaseAST>?) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        for (ast in params!!) {
            ast.accept(visitor)
        }
        visitor.visit(this)
    }
}
