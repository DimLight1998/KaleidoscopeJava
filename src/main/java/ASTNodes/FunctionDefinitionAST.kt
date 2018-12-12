package ASTNodes

class FunctionDefinitionAST(var prototype: FunctionPrototypeAST?, var body: BaseAST?) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        prototype!!.accept(visitor)
        body!!.accept(visitor)
        visitor.visit(this)
    }
}
