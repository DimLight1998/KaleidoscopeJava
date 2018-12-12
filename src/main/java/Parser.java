import AstNodes.*;
import sun.awt.X11.ToplevelStateListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("Duplicates")
public class Parser {
    private HashMap<Character, Integer> binOpPrecedence;
    private int progress = 0;
    private List<Lexer.Token> tokens;

    private int getPrecedence(char op) {
        int ret = binOpPrecedence.getOrDefault(op, -1);
        return ret > 0 ? ret : -1;
    }

    public Parser() {
        // build precedence table
        binOpPrecedence = new HashMap<>();
        binOpPrecedence.put('<', 10);
        binOpPrecedence.put('+', 20);
        binOpPrecedence.put('-', 20);
        binOpPrecedence.put('*', 40);
    }

    public List<BaseAst> parse(List<Lexer.Token> tokens) {
        ArrayList<BaseAst> ret = new ArrayList<>();
        this.tokens = tokens;
        progress = 0;

        while (progress < tokens.size()) {
            switch (tokens.get(progress).tokenType) {
                case EOF:
                    return ret;
                case DEF:
                    ret.add(parseFunctionDef());
                    break;
                case EXTERN:
                    ret.add(parseExtern());
                    break;
                case SEMICOLON:
                    progress++;
                    break;
                default:
                    ret.add(parseTopLevelExpr());
                    break;
            }
        }
        System.err.println("WARN: no EOF");
        return ret;
    }

    private BaseAst parseNumber() {
        Lexer.Token curr = tokens.get(progress);
        if (curr.tokenType == Lexer.TokenType.NUMBER) {
            double value = Double.valueOf(curr.text);
            NumberAst ret = new NumberAst(value);
            progress++;
            return ret;
        } else {
            System.err.println("expect number, got " + curr.text);
            return null;
        }
    }

    private BaseAst parseParen() {
        Lexer.Token curr = tokens.get(progress);
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text.equals("("))) {
            System.err.println("expect '(', got " + curr.text);
        }
        progress++;

        BaseAst ret = parseExpression();

        curr = tokens.get(progress);
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text.equals(")"))) {
            System.err.println("expect ')', got " + curr.text);
        }
        progress++;
        return ret;
    }

    private BaseAst parseIdentifier() {
        Lexer.Token curr = tokens.get(progress);
        if (curr.tokenType != Lexer.TokenType.IDENTIFIER) {
            System.err.println("expect an identifier, got " + curr.text);
            return null;
        }

        // peek next token
        if (progress < tokens.size() - 1) {
            Lexer.Token peek = tokens.get(progress + 1);
            if (peek.tokenType == Lexer.TokenType.OTHER && peek.text.equals("(")) {
                // should be a function call
                progress += 2;
                List<BaseAst> args = new ArrayList<>();
                while (true) {
                    BaseAst arg = parseExpression();
                    args.add(arg);

                    if (tokens.get(progress).tokenType == Lexer.TokenType.OTHER &&
                            tokens.get(progress).text.equals(")")) {
                        progress++;
                        return new FunctionCallAst(curr.text, args);
                    }

                    if (tokens.get(progress).tokenType != Lexer.TokenType.OTHER ||
                            !tokens.get(progress).text.equals(",")) {
                        System.err.println("expect ')' or ',', got " + curr.text);
                        return null;
                    }

                    // is a ','
                    progress++;
                }
            }
        }

        // otherwise it's just a variable
        progress++;
        return new VariableAst(curr.text);
    }

    private BaseAst parsePrimary() {
        Lexer.Token curr = tokens.get(progress);
        switch (curr.tokenType) {
            case IDENTIFIER:
                return parseIdentifier();
            case NUMBER:
                return parseNumber();
            case OTHER:
                if (curr.text.equals("(")) return parseParen();
                // fall!
            default:
                System.err.println("expect a primary, got " + curr.text);
                return null;
        }
    }

    private BaseAst parseBinOpRhs(int minPrecedence, BaseAst lhs) {
        while (true) {
            // end of token stream, return lhs
            if (progress == tokens.size()) return lhs;
            Lexer.Token curr = tokens.get(progress);
            if (curr.tokenType != Lexer.TokenType.OTHER) return lhs;

            // now this is a operator (should be)
            char operation = curr.text.charAt(0);
            int precedence = getPrecedence(operation);
            if (precedence < minPrecedence) return lhs;
            progress++;
            BaseAst rhs = parsePrimary();
            if (rhs == null) return null;
            curr = tokens.get(progress);
            char nextOperation = curr.text.charAt(0);
            int nextPrecedence = getPrecedence(nextOperation);
            if (precedence < nextPrecedence) {
                rhs = parseBinOpRhs(precedence + 1, rhs);
                if (rhs == null) return null;
            }

            lhs = new BinaryExprAst(lhs, rhs, operation);
        }
    }

    private BaseAst parseExpression() {
        BaseAst lhs = parsePrimary();
        if (lhs == null) return null;
        return parseBinOpRhs(0, lhs);
    }

    private BaseAst parsePrototype() {
        Lexer.Token curr = tokens.get(progress);
        if (curr.tokenType != Lexer.TokenType.IDENTIFIER) {
            System.err.println("expect an identifier, got " + curr.text);
            return null;
        }

        String functionName = curr.text;
        progress++;
        curr = tokens.get(progress);
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text.equals("("))) {
            System.err.println("expect '(', got " + curr.text);
        }
        progress++;
        List<String> args = new ArrayList<>();
        while (true) {
            Lexer.Token arg = tokens.get(progress);
            if (arg.tokenType != Lexer.TokenType.IDENTIFIER) {
                break;
            }
            args.add(arg.text);
            progress++;
        }
        curr = tokens.get(progress);
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text.equals(")"))) {
            System.err.println("expect ')', got " + curr.text);
        }
        progress++;
        return new FunctionProtoAst(functionName, args);
    }

    private BaseAst parseFunctionDef() {
        Lexer.Token curr = tokens.get(progress);
        if (curr.tokenType != Lexer.TokenType.DEF) {
            System.err.println("expect DEF, got " + curr.text);
            return null;
        }
        progress++;
        BaseAst proto = parsePrototype();
        if (proto == null) return null;
        BaseAst expr = parseExpression();
        if (expr == null) return null;
        return new FunctionDefAst((FunctionProtoAst) proto, expr);
    }

    private BaseAst parseTopLevelExpr() {
        BaseAst expr = parseExpression();
        if (expr == null) return null;
        FunctionProtoAst proto = new FunctionProtoAst("__anonymous__", new ArrayList<>());
        return new FunctionDefAst(proto, expr);
    }

    private BaseAst parseExtern() {
        Lexer.Token curr = tokens.get(progress);
        if (curr.tokenType != Lexer.TokenType.EXTERN) {
            System.err.println("expect EXTERN, got " + curr.text);
            return null;
        }

        progress++;
        return parsePrototype();
    }
}
