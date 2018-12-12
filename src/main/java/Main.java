import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Lexer lexer = new Lexer();
        try {
            String content = new String(Files.readAllBytes(Paths.get("misc/test.k")), StandardCharsets.US_ASCII);
            List<Lexer.Token> list =  lexer.GetTokens(content);
            System.out.println(list);
        } catch (IOException e) {
            System.err.println("IO exception!");
        }
    }
}
