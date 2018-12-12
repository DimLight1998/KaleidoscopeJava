package astNodes

class FunctionPrototypeAST(var functionName: String, var params: List<String>) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
