package Aether;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class aether {

    // hashmap to store keywords and their token types for easy lookup when scanning
    // identifiers
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("and", TokenType.AND);
        KEYWORDS.put("class", TokenType.CLASS);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("fun", TokenType.FUN);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("nil", TokenType.NIL);
        KEYWORDS.put("or", TokenType.OR);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("super", TokenType.SUPER);
        KEYWORDS.put("this", TokenType.THIS);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("var", TokenType.VAR);
        KEYWORDS.put("while", TokenType.WHILE);
    }

    // some private global variables to use during the scanning process to keep
    // track of where we are in the source code and what tokens we have found
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // constructor to initialize the scanner with the source code in the language
    public aether(String source) {
        this.source = source;
    }

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.out.println("No arguments detected, entering AetherTest mode...");
            runATest();
        } else if (args.length == 1) {
            // Read the entire file into a string
            byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
            // Run the scanner on the file content
            run(new String(bytes, StandardCharsets.UTF_8));
        } else {
            System.out.println("Too many arguments detected, exiting...");
        }
    }

    private static void runATest() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            // Display a prompt for user input infinitely until the user exits
            System.out.print("AetherTest> ");

            // scans the line the user inputs and runs the scanner on it
            String line = scanner.nextLine();

            // if the user inputs nothing, we break out of the loop and exit the program
            if (line == null)
                break;
            run(line);
        }
    }

    private static void run(String source) {
        // Creates a scanner to scan the source code and generate the tokens in the code
        aether scanner = new aether(source);

        // runs the scan tokens method to generate the tokens and stores them in a list
        List<Token> tokens = scanner.scanTokens();

        // prints each token in the source code
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public List<Token> scanTokens() {
        // while the scanner has not reached the end of the source code string, we scan
        // the next token
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        // once we reach the end of the source code we end an EOF token to signify the
        // end of the source code and return the list
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        // read the token character by character and determine the token type with the
        // switch statement

        char c = advance();
        switch (c) {
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;

            // Two-character operators
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;
            case '>':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;

            // Slash or single-line comment
            case '/':
                if (match('/')) {
                    // Comments extend until the end of the line
                    while (peek() != '\n' && !isAtEnd())
                        advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Whitespace
            case ' ':
            case '\r':
            case '\t':
                break;

            case '\n':
                // Increment line number for new lines to keep accurate track of where the
                // tokens are located at (error reporting)
                line++;
                break;

            // Literals
            case '"':
                string();
                break;

            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // Reports an error to the user if the scanner runs into a character that does not belong syntactically in the language
                    scannerError(line, "Unexpected character: " + c);
                }
                break;
        }
    }

    // Helper methods for scanning tokens

    private void identifier() {
        while (isAlphaNumeric(peek()))
            advance();

        String text = source.substring(start, current);
        TokenType type = KEYWORDS.get(text);
        if (type == null)
            type = TokenType.IDENTIFIER;

        addToken(type);
    }

    private void number() {
        while (isDigit(peek()))
            advance();

        // Look for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume the "."
            advance();

            while (isDigit(peek()))
                advance();
        }

        addToken(TokenType.NUMBER, source.substring(start, current));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n')
                line++;
            advance();
        }

        if (isAtEnd()) {
            //reports an error for a string that did not have a final closing quote and was unterimnated.
            scannerError(line, "Unterminated string.");
            return;
        }

        // The closing quote.
        advance();

        // Trim quotes to store exact string literal content
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    //Helper method to check if the next character in the source code matches the expected character (used for two-character tokens like !=, ==, <=, >=)
    private boolean match(char expected) {
        if (isAtEnd())
            return false;
        if (source.charAt(current) != expected)
            return false;

        current++;
        return true;
    }

    // Helper methods to look ahead to the next character in the sequence without moving fully to it 
    // (used for checking if the next character is a digit or letter when scanning numbers and identifiers)
    private char peek() {
        if (isAtEnd())
            return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length())
            return '\0';
        return source.charAt(current + 1);
    }


    // Helper methods to check if a character is a letter, digit, or alphanumeric (used for scanning identifiers and numbers)
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    // Helper method to check if a character is alphanumeric (used for scanning identifiers)
    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }


    // Helper method to check if a character is a digit (used for scanning numbers)
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }


    // Helper method to check if we have reached the end of the source code string (used to determine when to stop scanning)
    private boolean isAtEnd() {
        return current >= source.length();
    }

    // move to the next character in the source code
    private char advance() {
        return source.charAt(current++);
    }

    // create a token (for simple single character tokens like a parenthesis or a
    // semicolon) and add it to the list of tokens
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // adds the full token to the list of tokens
    private void addToken(TokenType type, String literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    // error reporting to help report where the error is and what lead to the error
    private static void scannerError(int line, String message) {
        System.err.println("[line " + line + "] Scanner Error: " + message);
    }
}