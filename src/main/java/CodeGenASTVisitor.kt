import astNodes.*

import org.bytedeco.javacpp.LLVM.*

import org.bytedeco.javacpp.PointerPointer

import java.util.*

class CodeGenASTVisitor : ASTVisitor() {
    private val valueRefStack = Stack<LLVMValueRef>()
    private val namedValues = HashMap<String, LLVMValueRef>()
    private val module = LLVMModuleCreateWithName("my_module")
    private val builder = LLVMCreateBuilder()

    override fun visit(ast: BaseAST) {
        throw Exception("this method should not be called")
    }

    fun dump() {
        LLVMDumpModule(module)
    }

    override fun visit(ast: BinaryExpressionAST) {
        ast.left.accept(this)
        val left = valueRefStack.pop()
        ast.right.accept(this)
        val right = valueRefStack.pop()
        val pushing = when (ast.operation) {
            '+' -> LLVMBuildFAdd(builder, left, right, "addtmp")
            '-' -> LLVMBuildFSub(builder, left, right, "subtmp")
            '*' -> LLVMBuildFMul(builder, left, right, "multmp")
            '<' -> {
                val value = LLVMBuildFCmp(builder, LLVMRealULT, left, right, "cmptmp")
                LLVMBuildUIToFP(builder, value, LLVMDoubleType(), "booltmp")
            }
            else -> {
                throw Exception("unknown operation")
            }
        }
        valueRefStack.push(pushing)
    }

    override fun visit(ast: FunctionCallAST) {
        val func = LLVMGetNamedFunction(module, ast.functionName)
        if (LLVMCountParams(func) != ast.params.size) {
            System.err.println("unmatched number of params")
            return
        }

        val args = Array<LLVMValueRef>(Math.max(1, ast.params.size)) {
            ast.params[it].accept(this)
            val value = valueRefStack.pop()
            value
        }

        valueRefStack.push(LLVMBuildCall(builder, func, PointerPointer(*args), ast.params.size, "calltmp"))
    }

    override fun visit(ast: FunctionDefinitionAST) {
        namedValues.clear()
        ast.prototype.accept(this)
        val function = valueRefStack.pop()
        LLVMPositionBuilderAtEnd(builder, LLVMAppendBasicBlock(function, "entry"))
        ast.body.accept(this)
        val body = valueRefStack.pop()
        LLVMBuildRet(builder, body)
        LLVMVerifyFunction(function, LLVMPrintMessageAction)
        valueRefStack.push(function)
    }

    override fun visit(ast: FunctionPrototypeAST) {
        val size = ast.params.size
        val doubles = Array<LLVMTypeRef>(size) { LLVMDoubleType() }
        val functionType = LLVMFunctionType(LLVMDoubleType(), PointerPointer(*doubles), size, 0)
        val function = LLVMAddFunction(module, ast.functionName, functionType)
        LLVMSetLinkage(function, LLVMExternalLinkage)

        for (i in 0 until size) {
            val argName = ast.params[i]
            val param = LLVMGetParam(function, i)
            LLVMSetValueName(param, argName)
            namedValues[argName] = param
        }

        valueRefStack.push(function)
    }

    override fun visit(ast: NumberAST) {
        valueRefStack.push(LLVMConstReal(LLVMDoubleType(), ast.value))
    }

    override fun visit(ast: VariableAST) {
        valueRefStack.push(namedValues[ast.variableName])
    }

    override fun visit(ast: IfExpressionAST) {
        ast.condition.accept(this)
        var condition = valueRefStack.pop()
        val zero = LLVMConstReal(LLVMDoubleType(), 0.0)
        condition = LLVMBuildFCmp(builder, LLVMRealONE, condition, zero, "ifcond")
        val function = LLVMGetBasicBlockParent(LLVMGetInsertBlock(builder))

        var positiveBB = LLVMAppendBasicBlock(function, "then")
        var negativeBB = LLVMAppendBasicBlock(function, "else")
        val mergeBB = LLVMAppendBasicBlock(function, "ifcont")
        LLVMBuildCondBr(builder, condition, positiveBB, negativeBB)

        LLVMPositionBuilderAtEnd(builder, positiveBB)
        ast.positive.accept(this)
        val positive = valueRefStack.pop()
        LLVMBuildBr(builder, mergeBB)
        positiveBB = LLVMGetInsertBlock(builder)

        LLVMPositionBuilderAtEnd(builder, negativeBB)
        ast.negative.accept(this)
        val negative = valueRefStack.pop()
        LLVMBuildBr(builder, mergeBB)
        negativeBB = LLVMGetInsertBlock(builder)

        LLVMPositionBuilderAtEnd(builder, mergeBB)
        val phi = LLVMBuildPhi(builder, LLVMDoubleType(), "if_tmp")
        LLVMAddIncoming(
                phi,
                PointerPointer(*Array<LLVMValueRef>(1) { positive }),
                PointerPointer(*Array<LLVMBasicBlockRef>(1) { positiveBB }),
                1
        )
        LLVMAddIncoming(
                phi,
                PointerPointer(*Array<LLVMValueRef>(1) { negative }),
                PointerPointer(*Array<LLVMBasicBlockRef>(1) { negativeBB }),
                1
        )
        valueRefStack.push(phi)
    }
}
