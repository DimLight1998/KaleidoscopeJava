import ASTNodes.*;

import static org.bytedeco.javacpp.LLVM.*;

import org.bytedeco.javacpp.PointerPointer;

import java.util.*;

public class CodeGenASTVisitor extends ASTVisitor {
    private Stack<LLVMValueRef> valueRefStack = new Stack<>();
    private Map<String, LLVMValueRef> namedValues = new HashMap<>();
    private LLVMModuleRef module = LLVMModuleCreateWithName("my_module");
    private LLVMBuilderRef builder = LLVMCreateBuilder();

    @Override
    public void visit(BaseAST ast) {

    }

    public void Dump() {
        LLVMDumpModule(module);
    }

    @Override
    public void visit(BinaryExpressionAST ast) {
        LLVMValueRef right = valueRefStack.pop();
        LLVMValueRef left = valueRefStack.pop();
        LLVMValueRef pushing;
        switch (ast.getOperation()) {
            case '+':
                pushing = LLVMBuildFAdd(builder, left, right, "addtmp");
                break;
            case '-':
                pushing = LLVMBuildFSub(builder, left, right, "subtmp");
                break;
            case '*':
                pushing = LLVMBuildFMul(builder, left, right, "multmp");
                break;
            case '<':
                LLVMValueRef value = LLVMBuildFCmp(builder, LLVMRealULT, left, right, "cmptmp");
                pushing = LLVMBuildUIToFP(builder, value, LLVMDoubleType(), "booltmp");
                break;
            default:
                System.err.println("unknown operation");
                pushing = null;
                break;
        }
        valueRefStack.push(pushing);
    }

    @Override
    public void visit(FunctionCallAST ast) {
        LLVMValueRef func = LLVMGetNamedFunction(module, ast.getFunctionName());
        if (func.isNull()) {
            System.err.println("unknown function");
            return;
        }

        if (LLVMCountParams(func) != ast.getParams().size()) {
            System.err.println("unmatched number of params");
            return;
        }

        LLVMValueRef[] args = new LLVMValueRef[Math.max(1, ast.getParams().size())];
        for (int i = 0; i < ast.getParams().size(); i++) {
            LLVMValueRef value = valueRefStack.pop();
            args[i] = value;
        }

        valueRefStack.push(LLVMBuildCall(builder, func, new PointerPointer(args), ast.getParams().size(), "calltmp"));
    }

    @Override
    public void visit(FunctionDefinitionAST ast) {
        // this is a prototype
        LLVMValueRef body = valueRefStack.pop();
        LLVMValueRef prototype = valueRefStack.pop();

        LLVMPositionBuilderAtEnd(builder, LLVMAppendBasicBlock(prototype, "entry"));
        LLVMBuildRet(builder, body);
        LLVMVerifyFunction(prototype, LLVMPrintMessageAction);
        valueRefStack.push(prototype);
    }

    @Override
    public void visit(FunctionPrototypeAST ast) {
        LLVMTypeRef[] doubles = new LLVMTypeRef[ast.getParams().size()];
        for (int i = 0; i < ast.getParams().size(); i++) {
            doubles[i] = LLVMDoubleType();
        }
        LLVMTypeRef functionType = LLVMFunctionType(LLVMDoubleType(), new PointerPointer(doubles), ast.getParams().size(), 0);
        LLVMValueRef function = LLVMAddFunction(module, ast.getFunctionName(), functionType);
        LLVMSetLinkage(function, LLVMExternalLinkage);

        for (int i = 0; i < ast.getParams().size(); i++) {
            String argName = ast.getParams().get(i);
            LLVMValueRef param = LLVMGetParam(function, i);
            LLVMSetValueName(param, argName);
            namedValues.put(argName, param);
        }

        valueRefStack.push(function);
    }

    @Override
    public void visit(NumberAST ast) {
        valueRefStack.push(LLVMConstReal(LLVMDoubleType(), ast.getValue()));
    }

    @Override
    public void visit(VariableAST ast) {
        valueRefStack.push(namedValues.getOrDefault(ast.getVariableName(), null));
    }
}
