package astNodes

class FunctionDefinitionAST(var prototype: FunctionPrototypeAST?, var body: BaseAST?) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
