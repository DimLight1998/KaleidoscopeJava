import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val lexer = Lexer(false)
        val parser = Parser()
        try {
            val content = String(Files.readAllBytes(Paths.get("misc/test.k")), StandardCharsets.US_ASCII)
            val list = lexer.getTokens(content)
            val rets = parser.parse(list)
            val visitor = CodeGenASTVisitor()
            for (ret in rets) {
                ret.accept(visitor)
            }
            visitor.dump()
        } catch (e: IOException) {
            System.err.println("IO exception!")
        }

    }
}
