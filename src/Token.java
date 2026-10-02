package Aether;

enum TokenType {
    // Define token types here
    
    //Single-character tokens
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    //ONE OR TWO CHARACTER TOKENS
    BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, 
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL, 

    //lITERALS
    IDENTIFIER, STRING, NUMBER,

    //kEYWORDS
    AND, CLASS, ELSE, FALSE, FUN, FOR, IF, NIL, OR,
    PRINT, RETURN, SUPER, THIS, TRUE, VAR, WHILE,

    //END OF FILE
    EOF
}

public class Token {
    TokenType TOKENTYPE;
    String lexeme;
    String literal;
    int line;

    public Token(TokenType TOKENTYPE, String lexeme, String literal, int line) {
        this.TOKENTYPE = TOKENTYPE;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public TokenType getType() {
        return TOKENTYPE;
    }

    public String getLexeme() {
        return lexeme;
    }

    public String getLiteralValue() {
        return literal;
    }

    public int getLine() {
        return line;
    }

    public String toString() {
        return TOKENTYPE + " " + lexeme + " " + literal + " " + line;
    }
}
