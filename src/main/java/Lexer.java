import java.util.*;

class Lexer {
    enum TokenType {
        EOF, DEF, EXTERN, IDENTIFIER, COMMENT, NUMBER, SEMICOLON, OTHER
    }

    private Map<String, TokenType> keywords;
    private boolean preserveComments = false;

    Lexer(boolean preserveComments) {
        // initialize keywords
        this.keywords = new HashMap<>();
        this.keywords.put("def", TokenType.DEF);
        this.keywords.put("extern", TokenType.EXTERN);
        this.preserveComments = preserveComments;
    }

    class Token {
        TokenType tokenType;
        String text;

        Token(TokenType tokenType, String text) {
            this.tokenType = tokenType;
            this.text = text;
        }
    }

    List<Token> GetTokens(String sourceText) {
        int pos = 0;
        ArrayList<Token> ret = new ArrayList<>();

        while (pos < sourceText.length()) {
            // skip whitespace and test EOF
            while (pos < sourceText.length() && Character.isWhitespace(sourceText.charAt(pos))) {
                pos++;
            }
            if (pos == sourceText.length()) {
                break;
            }

            if (Character.isAlphabetic(sourceText.charAt(pos))) {
                // is this a key word or identifier?
                StringBuilder stringBuilder = new StringBuilder();
                while (pos < sourceText.length() && Character.isLetterOrDigit(sourceText.charAt(pos))) {
                    stringBuilder.append(sourceText.charAt(pos));
                    pos++;
                }

                String s = stringBuilder.toString();
                if (this.keywords.keySet().contains(s)) {
                    ret.add(new Token(this.keywords.get(s), s));
                } else {
                    ret.add(new Token(TokenType.IDENTIFIER, s));
                }
            } else if (Character.isDigit(sourceText.charAt(pos)) || sourceText.charAt(pos) == '.') {
                // is this a value?
                StringBuilder stringBuilder = new StringBuilder();
                while (pos < sourceText.length() && (
                        Character.isDigit(sourceText.charAt(pos)) || sourceText.charAt(pos) == '.'
                )) {
                    stringBuilder.append(sourceText.charAt(pos));
                    pos++;
                }

                ret.add(new Token(TokenType.NUMBER, stringBuilder.toString()));
            } else if (sourceText.charAt(pos) == '#') {
                // is this a comment?
                StringBuilder stringBuilder = new StringBuilder();
                while (pos < sourceText.length() && (sourceText.charAt(pos) != '\r' && sourceText.charAt(pos) != '\n')) {
                    stringBuilder.append(sourceText.charAt(pos));
                    pos++;
                }
                if (preserveComments) {
                    ret.add(new Token(TokenType.COMMENT, stringBuilder.toString()));
                }
            } else if (sourceText.charAt(pos) == ';') {
                ret.add(new Token(TokenType.SEMICOLON, ";"));
                pos++;
            } else {
                ret.add(new Token(TokenType.OTHER, String.valueOf(sourceText.charAt(pos))));
                pos++;
            }
        }
        ret.add(new Token(TokenType.EOF, ""));
        return ret;
    }
}
