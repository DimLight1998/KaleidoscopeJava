package astNodes

class VariableAST(var variableName: String?) : BaseAST() {

    override fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
