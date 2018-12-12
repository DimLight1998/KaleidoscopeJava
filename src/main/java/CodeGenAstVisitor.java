import AstNodes.*;

import static org.bytedeco.javacpp.LLVM.*;

import org.bytedeco.javacpp.PointerPointer;

import java.util.*;

public class CodeGenAstVisitor extends AstVisitor {
    private Stack<LLVMValueRef> valueRefStack = new Stack<>();
    private Map<String, LLVMValueRef> namedValues = new HashMap<>();
    private LLVMModuleRef module = LLVMModuleCreateWithName("my_module");
    private LLVMBuilderRef builder = LLVMCreateBuilder();

    @Override
    public void visit(BaseAst ast) {

    }

    public void Dump() {
        LLVMDumpModule(module);
    }

    @Override
    public void visit(BinaryExprAst ast) {
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
    public void visit(FunctionCallAst ast) {
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
    public void visit(FunctionDefAst ast) {
        // this is a proto
        LLVMValueRef body = valueRefStack.pop();
        LLVMValueRef proto = valueRefStack.pop();

        LLVMPositionBuilderAtEnd(builder, LLVMAppendBasicBlock(proto, "entry"));
        LLVMBuildRet(builder, body);
        LLVMVerifyFunction(proto, LLVMPrintMessageAction);
        valueRefStack.push(proto);
    }

    @Override
    public void visit(FunctionProtoAst ast) {
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
    public void visit(NumberAst ast) {
        valueRefStack.push(LLVMConstReal(LLVMDoubleType(), ast.getValue()));
    }

    @Override
    public void visit(VariableAst ast) {
        valueRefStack.push(namedValues.getOrDefault(ast.getVariableName(), null));
    }
}
