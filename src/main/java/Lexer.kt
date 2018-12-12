import java.util.*

class Lexer(preserveComments: Boolean) {

    private val keywords: MutableMap<String, TokenType>
    private var preserveComments = false

    enum class TokenType {
        EOF, DEF, EXTERN, IDENTIFIER, COMMENT, NUMBER, SEMICOLON, OTHER,
        IF, THEN, ELSE
    }

    init {
        // initialize keywords
        this.keywords = hashMapOf(
                "def" to TokenType.DEF,
                "extern" to TokenType.EXTERN,
                "if" to TokenType.IF,
                "then" to TokenType.THEN,
                "else" to TokenType.ELSE
        )
        this.preserveComments = preserveComments
    }

    class Token(var tokenType: TokenType, var text: String)

    fun getTokens(sourceText: String): List<Token> {
        var pos = 0
        val ret = ArrayList<Token>()

        while (pos < sourceText.length) {
            // skip whitespace and test EOF
            while (pos < sourceText.length && Character.isWhitespace(sourceText[pos])) {
                pos++
            }
            if (pos == sourceText.length) {
                break
            }

            if (Character.isAlphabetic(sourceText[pos].toInt())) {
                // is this a key word or identifier?
                val stringBuilder = StringBuilder()
                while (pos < sourceText.length && Character.isLetterOrDigit(sourceText[pos])) {
                    stringBuilder.append(sourceText[pos])
                    pos++
                }

                val s = stringBuilder.toString()
                if (s in this.keywords.keys) {
                    ret.add(Token(this.keywords[s]!!, s))
                } else {
                    ret.add(Token(TokenType.IDENTIFIER, s))
                }
            } else if (Character.isDigit(sourceText[pos]) || sourceText[pos] == '.') {
                // is this a value?
                val stringBuilder = StringBuilder()
                while (pos < sourceText.length && (Character.isDigit(sourceText[pos]) || sourceText[pos] == '.')) {
                    stringBuilder.append(sourceText[pos])
                    pos++
                }

                ret.add(Token(TokenType.NUMBER, stringBuilder.toString()))
            } else if (sourceText[pos] == '#') {
                // is this a comment?
                val stringBuilder = StringBuilder()
                while (pos < sourceText.length && sourceText[pos] != '\r' && sourceText[pos] != '\n') {
                    stringBuilder.append(sourceText[pos])
                    pos++
                }
                if (preserveComments) {
                    ret.add(Token(TokenType.COMMENT, stringBuilder.toString()))
                }
            } else if (sourceText[pos] == ';') {
                ret.add(Token(TokenType.SEMICOLON, ";"))
                pos++
            } else {
                ret.add(Token(TokenType.OTHER, sourceText[pos].toString()))
                pos++
            }
        }
        ret.add(Token(TokenType.EOF, ""))
        return ret
    }
}
