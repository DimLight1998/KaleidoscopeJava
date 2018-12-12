import astNodes.*

import java.util.ArrayList
import java.util.HashMap

class Parser {
    private val binOpPrecedence: MutableMap<Char, Int> = HashMap()
    private var progress = 0
    private lateinit var tokens: List<Lexer.Token>;

    private fun getPrecedence(op: Char): Int {
        val ret = binOpPrecedence.getOrDefault(op, -1)
        return if (ret > 0) ret else -1
    }

    init {
        // build precedence table
        binOpPrecedence['<'] = 10
        binOpPrecedence['+'] = 20
        binOpPrecedence['-'] = 20
        binOpPrecedence['*'] = 40
    }

    fun parse(tokens: List<Lexer.Token>): List<BaseAST> {
        val ret = ArrayList<BaseAST>()
        this.tokens = tokens
        progress = 0

        while (progress < tokens.size) {
            when (tokens[progress].tokenType) {
                Lexer.TokenType.EOF -> return ret
                Lexer.TokenType.DEF -> ret.add(parseFunctionDef())
                Lexer.TokenType.EXTERN -> ret.add(parseExtern())
                Lexer.TokenType.SEMICOLON -> progress++
                else -> ret.add(parseTopLevelExpr())
            }
        }
        System.err.println("WARN: no EOF")
        return ret
    }

    private fun parseNumber(): BaseAST {
        val curr = tokens[progress]
        return if (curr.tokenType == Lexer.TokenType.NUMBER) {
            val value = java.lang.Double.valueOf(curr.text)
            val ret = NumberAST(value)
            progress++
            ret
        } else {
            throw Exception("expect number, got " + curr.text)
        }
    }

    private fun parseParen(): BaseAST {
        var curr: Lexer.Token = tokens[progress]
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text == "(")) {
            System.err.println("expect '(', got " + curr.text)
        }
        progress++

        val ret = parseExpression()

        curr = tokens[progress]
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text == ")")) {
            System.err.println("expect ')', got " + curr.text)
        }
        progress++
        return ret
    }

    private fun parseIdentifier(): BaseAST {
        val curr = tokens[progress]
        if (curr.tokenType != Lexer.TokenType.IDENTIFIER) {
            throw Exception("expect an identifier, got " + curr.text)
        }

        // peek next token
        if (progress < tokens.size - 1) {
            val peek = tokens[progress + 1]
            if (peek.tokenType == Lexer.TokenType.OTHER && peek.text == "(") {
                // should be a function call
                progress += 2
                val args = ArrayList<BaseAST>()
                while (true) {
                    val arg = parseExpression()
                    args.add(arg)

                    if (tokens[progress].tokenType == Lexer.TokenType.OTHER && tokens[progress].text == ")") {
                        progress++
                        return FunctionCallAST(curr.text, args)
                    }

                    if (tokens[progress].tokenType != Lexer.TokenType.OTHER || tokens[progress].text != ",") {
                        throw Exception("expect ')' or ',', got " + curr.text)
                    }

                    // is a ','
                    progress++
                }
            }
        }

        // otherwise it's just a variable
        progress++
        return VariableAST(curr.text)
    }

    private fun parsePrimary(): BaseAST {
        val curr = tokens[progress]
        when (curr.tokenType) {
            Lexer.TokenType.IDENTIFIER -> return parseIdentifier()
            Lexer.TokenType.NUMBER -> return parseNumber()
            Lexer.TokenType.IF -> return parseIfExpr()
            Lexer.TokenType.OTHER -> {
                if (curr.text == "(") return parseParen()
                throw Exception("expect a primary, got " + curr.text)
            }
            else -> {
                throw Exception("expect a primary, got " + curr.text)
            }
        }
    }

    private fun parseBinOpRhs(minPrecedence: Int, lhs: BaseAST): BaseAST {
        var lhs1 = lhs
        while (true) {
            // end of token stream, return lhs
            if (progress == tokens.size) return lhs1
            var curr: Lexer.Token = tokens[progress]
            if (curr.tokenType != Lexer.TokenType.OTHER) return lhs1

            // now this is a operator (should be)
            val operation = curr.text[0]
            val precedence = getPrecedence(operation)
            if (precedence < minPrecedence) return lhs1
            progress++
            var rhs = parsePrimary()
            curr = tokens[progress]
            val nextOperation = curr.text[0]
            val nextPrecedence = getPrecedence(nextOperation)
            if (precedence < nextPrecedence) {
                rhs = parseBinOpRhs(precedence + 1, rhs)
            }

            lhs1 = BinaryExpressionAST(lhs1, rhs, operation)
        }
    }

    private fun parseExpression(): BaseAST {
        val lhs = parsePrimary()
        return parseBinOpRhs(0, lhs)
    }

    private fun parsePrototype(): BaseAST {
        var curr: Lexer.Token = tokens[progress]
        if (curr.tokenType != Lexer.TokenType.IDENTIFIER) {
            throw Exception("expect an identifier, got " + curr.text)
        }

        val functionName = curr.text
        progress++
        curr = tokens[progress]
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text == "(")) {
            System.err.println("expect '(', got " + curr.text)
        }
        progress++
        val args = ArrayList<String>()
        while (true) {
            val arg = tokens[progress]
            if (arg.tokenType != Lexer.TokenType.IDENTIFIER) {
                break
            }
            args.add(arg.text)
            progress++
        }
        curr = tokens[progress]
        if (!(curr.tokenType == Lexer.TokenType.OTHER && curr.text == ")")) {
            System.err.println("expect ')', got " + curr.text)
        }
        progress++
        return FunctionPrototypeAST(functionName, args)
    }

    private fun parseFunctionDef(): BaseAST {
        val curr = tokens[progress]
        if (curr.tokenType != Lexer.TokenType.DEF) {
            throw Exception("expect DEF, got " + curr.text)
        }
        progress++
        val prototype = parsePrototype()
        val expr = parseExpression()
        return FunctionDefinitionAST(prototype as FunctionPrototypeAST, expr)
    }

    private fun parseTopLevelExpr(): BaseAST {
        val expr = parseExpression()
        val prototype = FunctionPrototypeAST("__anonymous__", ArrayList())
        return FunctionDefinitionAST(prototype, expr)
    }

    private fun parseExtern(): BaseAST {
        val curr = tokens[progress]
        if (curr.tokenType != Lexer.TokenType.EXTERN) {
            throw Exception("expect EXTERN, got " + curr.text)
        }

        progress++
        return parsePrototype()
    }

    private fun parseIfExpr(): BaseAST {
        progress++;
        val condition = parseExpression()
        if (tokens[progress].tokenType != Lexer.TokenType.THEN) {
            throw Exception("expect then, got " + tokens[progress].text)
        }
        progress++
        val positive = parseExpression()
        if (tokens[progress].tokenType != Lexer.TokenType.ELSE) {
            throw Exception("expect else, got " + tokens[progress].text)
        }
        progress++
        val negative = parseExpression()
        return IfExpressionAST(condition, positive, negative)
    }
}
