package astNodes

open class BaseAST {
    open fun accept(visitor: ASTVisitor) {
        visitor.visit(this)
    }
}
