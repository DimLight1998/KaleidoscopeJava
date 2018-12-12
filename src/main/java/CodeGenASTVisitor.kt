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

    }

    fun dump() {
        LLVMDumpModule(module)
    }

    override fun visit(ast: BinaryExpressionAST) {
        val right = valueRefStack.pop()
        val left = valueRefStack.pop()
        val pushing: LLVMValueRef?
        pushing = when (ast.operation) {
            '+' -> LLVMBuildFAdd(builder, left, right, "addtmp")
            '-' -> LLVMBuildFSub(builder, left, right, "subtmp")
            '*' -> LLVMBuildFMul(builder, left, right, "multmp")
            '<' -> {
                val value = LLVMBuildFCmp(builder, LLVMRealULT, left, right, "cmptmp")
                LLVMBuildUIToFP(builder, value, LLVMDoubleType(), "booltmp")
            }
            else -> {
                System.err.println("unknown operation")
                null
            }
        }
        valueRefStack.push(pushing)
    }

    override fun visit(ast: FunctionCallAST) {
        val func = LLVMGetNamedFunction(module, ast.functionName)
        if (func.isNull) {
            System.err.println("unknown function")
            return
        }

        if (LLVMCountParams(func) != ast.params!!.size) {
            System.err.println("unmatched number of params")
            return
        }

        val args = arrayOfNulls<LLVMValueRef>(Math.max(1, ast.params!!.size))
        for (i in 0 until ast.params!!.size) {
            val value = valueRefStack.pop()
            args[i] = value
        }

        valueRefStack.push(LLVMBuildCall(builder, func, PointerPointer(*args), ast.params!!.size, "calltmp"))
    }

    override fun visit(ast: FunctionDefinitionAST) {
        // this is a prototype
        val body = valueRefStack.pop()
        val prototype = valueRefStack.pop()

        LLVMPositionBuilderAtEnd(builder, LLVMAppendBasicBlock(prototype, "entry"))
        LLVMBuildRet(builder, body)
        LLVMVerifyFunction(prototype, LLVMPrintMessageAction)
        valueRefStack.push(prototype)
    }

    override fun visit(ast: FunctionPrototypeAST) {
        val doubles = arrayOfNulls<LLVMTypeRef>(ast.params!!.size)
        for (i in 0 until ast.params!!.size) {
            doubles[i] = LLVMDoubleType()
        }
        val functionType = LLVMFunctionType(LLVMDoubleType(), PointerPointer(*doubles), ast.params!!.size, 0)
        val function = LLVMAddFunction(module, ast.functionName, functionType)
        LLVMSetLinkage(function, LLVMExternalLinkage)

        for (i in 0 until ast.params!!.size) {
            val argName = ast.params!![i]
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
        valueRefStack.push(namedValues.getOrDefault(ast.variableName, null))
    }
}
