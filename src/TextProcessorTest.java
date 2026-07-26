import java.util.List;

public class TextProcessorTest {
    public static void main(String[] args) {
        TextProcessor tp = new TextProcessor();
        List<String> tokens = tp.tokenize("We are looking for a Senior Java Developer with C++ experience.");
        System.out.println(tokens);
    }
}