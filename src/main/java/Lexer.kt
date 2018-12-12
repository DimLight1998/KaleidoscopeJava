import java.util.*

internal class Lexer(preserveComments: Boolean) {

    private val keywords: MutableMap<String, TokenType>
    private val preserveComments = false

    internal enum class TokenType {
        EOF, DEF, EXTERN, IDENTIFIER, COMMENT, NUMBER, SEMICOLON, OTHER
    }

    init {
        // initialize keywords
        this.keywords = HashMap()
        this.keywords["def"] = TokenType.DEF
        this.keywords["extern"] = TokenType.EXTERN
        this.preserveComments = preserveComments
    }

    internal inner class Token(var tokenType: TokenType, var text: String)

    fun GetTokens(sourceText: String): List<Token> {
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
                if (this.keywords.keys.contains(s)) {
                    ret.add(Token(this.keywords[s], s))
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
