package astNodes

open class ASTVisitor {
    open fun visit(ast: BaseAST) {}

    open fun visit(ast: BinaryExpressionAST) {}

    open fun visit(ast: FunctionCallAST) {}

    open fun visit(ast: FunctionDefinitionAST) {}

    open fun visit(ast: FunctionPrototypeAST) {}

    open fun visit(ast: NumberAST) {}

    open fun visit(ast: VariableAST) {}

    open fun visit(ast: IfExpressionAST) {}

    open fun visit(ast: ForExpressionAST) {}
}
